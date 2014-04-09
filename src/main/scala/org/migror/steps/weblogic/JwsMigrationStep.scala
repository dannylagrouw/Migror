/*
 *  Copyright 2011 Danny Lagrouw
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 */
package org.migror.steps.weblogic

import japa.parser.ast.CompilationUnit
import java.util.regex.Pattern
import org.migror.model.{Context, Step}
import org.apache.commons.lang.StringUtils
import org.migror.internal.LangUtils
import japa.parser.ASTHelper
import org.migror.javaparser.{RenameCalledMethod, MigrationVisitor}
import org.migror.steps.ParseStep
import japa.parser.ast.`type`.{ClassOrInterfaceType, ReferenceType}
import japa.parser.ast.body._
import japa.parser.ast.expr.NameExpr
import japa.parser.ast.visitor.VoidVisitorAdapter
import japa.parser.ast.stmt._
import org.migror.model.weblogic.{WsdlWebService, JwsWebService}
import scala.collection.JavaConversions._

class JwsMigrationStep extends ParseStep {
  val DefaultNamespace = "http://www.openuri.org/"
  val PackagePattern: Pattern = Pattern.compile("package\\s*([^;]*)")

  val classChanger = Some(new MigrationVisitor {
    override def visit(cu: CompilationUnit, arg: Object) {
      addImports(cu, createImport("javax.jws.WebService"))
      super.visit(cu, arg)
    }

    override def visit(method: MethodDeclaration, arg: Object) {
      if (method.getThrows != null) {
        removeExceptionFromThrows(method)
      }

      if (method.getType.isInstanceOf[ReferenceType]) {
        correctArrays(method)
      }

      if (method.getParameters != null && (method.getModifiers & ModifierSet.PUBLIC) > 0) {
        correctDateParameters(method)
      }
        		
      super.visit(method, arg)
    }

    private def findTargetNameSpace(decl: ClassOrInterfaceDeclaration): String = {
      getJavadocLineWith(decl, "@common:target-namespace") match {
        case Some(nameSpace) => nameSpace
        case None =>
          info("target-namespace is empty, using default namespace %s".format(DefaultNamespace))
          DefaultNamespace
      }
    }

    private def targetNameSpaceToSoapNameSpace(targetNameSpace: String): String = {
      StringUtils.stripEnd(targetNameSpace.replace("http://www.", "").replace("/", ""), "/").split("\\.").reverse.mkString(".").toLowerCase
    }

    private def findOriginalPackageName: String =
      LangUtils.withDo(PackagePattern.matcher(compilationUnit.getPackage.toString)) { matcher =>
          if (matcher.find) {
          matcher.group(1)
        } else {
          ""
        }
      }

    override def visit(decl: ClassOrInterfaceDeclaration, arg: Object) {
      val originalName = decl.getName
      val soapInterfaceName = originalName + "Soap";
      val orgPackage = findOriginalPackageName
      val targetNameSpace = findTargetNameSpace(decl)
      val soapNameSpace = targetNameSpaceToSoapNameSpace(targetNameSpace)
      val importToAdd = soapNameSpace + "." + soapInterfaceName

      addImplements(decl, soapInterfaceName)
      removeImplements(decl, "WebService")
      setName(decl, soapInterfaceName + "Impl")

      addImports(compilationUnit, createImport(importToAdd))

      addAnnotation(decl, createAnnotationWithNameValues("WebService",
          ("serviceName", soapInterfaceName),
          ("portName", soapInterfaceName + "Port"),
          ("targetNamespace", targetNameSpace),
          ("endpointInterface", importToAdd)))

      Context.add("jwswebservices",
        JwsWebService(decl.getName,
          orgPackage + "." + originalName , // original implementation name
          Some(orgPackage + "." + decl.getName), // new servlet class name fully qualified
          getJavadocLineWith(decl, "@common:security"))) //roles allowed comma seperated

      info("targetNamespace for %s = %s".format(originalName, targetNameSpace))
      info("implemented interface for %s = %s".format(originalName, importToAdd))

      // Alternatively use the WSDL name space from the parsed WSDL.
      // (This name space is deduced by WsdlCopyStep)
      val nameSpaceFromWsdl = getWsdlWebServiceFromContext(originalName).map(_.nameSpaceFromWsdl)
      nameSpaceFromWsdl match {
        case Some(nameSpace) if (nameSpace != soapNameSpace) =>
          warn("Target name space from annotation (%s) differs from WSDL name space (%s)".format(
          targetNameSpace, nameSpace))
        case _ => warn("WSDL for JWS %s probably missing".format(decl.getName))
      }

      super.visit(decl, arg)
    }

    private def getWsdlWebServiceFromContext(webserviceNaam: String): Option[WsdlWebService] = {
      val services: List[WsdlWebService] = Context.getList("wsdlwebservices")
      LangUtils.withDoReturn(services.find(_.webServiceName == webserviceNaam)) { service =>
        if (service.isEmpty) {
          warn("Web service %s not found in context.".format(webserviceNaam))
        }
      }
    }

    override def visit(decl: FieldDeclaration, arg: Object) {
      super.visit(decl, arg)
      migrateJwsContext(decl)
    }

    private def migrateJwsContext(decl: FieldDeclaration) {
      val JWS_CONTEXT_SIMPLE_CLASS_NAME = "JwsContext"
      val JWS_CONTEXT_CLASS_NAME = "com.bea.control." + JWS_CONTEXT_SIMPLE_CLASS_NAME
      val WS_CONTEXT_SIMPLE_CLASS_NAME = "WebServiceContext"
      val WS_CONTEXT_CLASS_NAME = "javax.xml.ws." + WS_CONTEXT_SIMPLE_CLASS_NAME

      val typeStr = decl.getType.toString
      val fieldName = decl.getVariables.get(0).getId.getName
      if (JWS_CONTEXT_SIMPLE_CLASS_NAME == typeStr || JWS_CONTEXT_CLASS_NAME == typeStr) {
        decl.setType(ASTHelper.createReferenceType(WS_CONTEXT_SIMPLE_CLASS_NAME, 0))
        addImports(compilationUnit, createImport(WS_CONTEXT_CLASS_NAME))
        removeImport(compilationUnit, JWS_CONTEXT_CLASS_NAME)

        cleanUpJavadoc(decl, "@common:context")
        addAnnotation(decl, createAnnotation("Resource"))
        addImports(compilationUnit, createImport("javax.annotation.Resource"))

        RenameCalledMethod(fieldName, "getCallerPrincipal", "getUserPrincipal").visit(compilationUnit, null)
      }
    }

    private def correctDateParameters(method: MethodDeclaration) {
      method.getParameters.foreach { param =>
        if (param.getType.isInstanceOf[ReferenceType]) {
          val refType = param.getType.asInstanceOf[ReferenceType]

          if (refType.getType.isInstanceOf[ClassOrInterfaceType]) {
            val classType = refType.getType.asInstanceOf[ClassOrInterfaceType]

            if (classType.getName == "Date") {
              val paramName = param.getId.getName
              refType.setType(new ClassOrInterfaceType("java.util.Calendar"))
              param.setId(new VariableDeclaratorId(paramName + "Param"))

              val expr = new NameExpr("java.util.Date %s = (%sParam != null ? %sParam.getTime() : null)".format(
                paramName, paramName, paramName))

              method.getBody.setStmts((new ExpressionStmt(expr)) :: method.getBody.getStmts.toList)
            }
          }
        }
      }
    }

    private def correctArrays(method: MethodDeclaration) {
      val refType = method.getType.asInstanceOf[ReferenceType]
      val dimensions = refType.getArrayCount

      if (dimensions > 0) {
        val classType = refType.getType.asInstanceOf[ClassOrInterfaceType]
        val isMultiDimension = (dimensions > 1)

        val newType = "%sArrayOf%s".format((if (isMultiDimension) "ArrayOf" else ""), classType.getName)

        refType.setType(new ClassOrInterfaceType(classType.getScope, newType))
        refType.setArrayCount(0)

        info("Return type for method %s changed from %s[]%s to %s".format(
          method.getName, classType.getName, (if (isMultiDimension) "[]" else ""), newType))

        method.getBody.accept(new VoidVisitorAdapter[Object]() {
          override def visit(returnStatement: ReturnStmt, arg: Object) {
            returnStatement.setExpr(new NameExpr("new %s(%s)".format(newType, returnStatement.getExpr.toString)))
            info("New return statement = " + returnStatement)
            super.visit(returnStatement, arg)
          }
        }, null)
      }
    }

    private def removeExceptionFromThrows(method: MethodDeclaration) {
      method.setThrows(method.getThrows.filterNot { expr =>

        if (expr.getName == "Exception") {
          val catchStatements = new BlockStmt
          val throwStatement = new ExpressionStmt(new NameExpr("throw new RuntimeException(ex)"))
          catchStatements.setStmts(List(throwStatement))

          val clause = new CatchClause(
            new Parameter(new ClassOrInterfaceType("Exception"), new VariableDeclaratorId("ex")), catchStatements)

          val statement = new TryStmt(method.getBody, List(clause), null)

          method.setBody(new BlockStmt(List(statement)))
          true
        } else {
          false
        }
      })

      if (method.getThrows.isEmpty) {
        method.setThrows(null);
      }
    }

  })
}

object JwsMigrationStep {
  def apply() = new JwsMigrationStep
}