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
package org.migror.javaparser

import japa.parser.ast.visitor.VoidVisitorAdapter
import japa.parser.ast.{ImportDeclaration, CompilationUnit}
import java.util.ArrayList
import scala.collection.JavaConversions._
import japa.parser.ast.expr._
import japa.parser.ast.`type`.ClassOrInterfaceType
import org.migror.internal.LangUtils.withDo
import java.util.regex.{Pattern, Matcher}
import japa.parser.ast.body._
import org.apache.commons.lang.StringUtils
import org.migror.internal.Logging

class MigrationVisitor extends VoidVisitorAdapter[Object] with Logging {

  /**
   * Adds an interface name to the list of implemented interfaces.
   */
  def addImplements(decl: ClassOrInterfaceDeclaration, implementedInterfaceName: String) {
    var impls = decl.getImplements()
    if (impls == null) {
      impls = new ArrayList[ClassOrInterfaceType]
      decl.setImplements(impls)
    }
    impls.add(new ClassOrInterfaceType(implementedInterfaceName))
  }

  /**
   * Removes an interface name from the list of implemented interfaces.
   */
  def removeImplements(decl: ClassOrInterfaceDeclaration, implementedInterfaceName: String) {
    val impls = decl.getImplements()
    if (impls != null) {
      decl.setImplements(nullIfEmpty(impls.filterNot(_.getName == implementedInterfaceName)))
    }
  }

  /**
   * Removes a throws clause for the specified exception class from
   * the method signature.
   */
  def removeThrows(decl: MethodDeclaration, exceptionName: String) {
    val throwsList = decl.getThrows
    if (throwsList != null) {
      decl.setThrows(nullIfEmpty(throwsList.filterNot(_.getName == exceptionName)))
    }
  }

  /**
   * Removes a class or interface name from the list of extended classes.
   *
   * @param decl
   * @param extendedClassName simple class name (without package name).
   */
  def removeExtends(decl: ClassOrInterfaceDeclaration, extendedClassName: String) {
    val extds = decl.getExtends

    if (extds != null) {
      decl.setExtends(nullIfEmpty(extds.filterNot(_.getName == extendedClassName)))
    }
  }

  /**
   * Maak een annotatie op basis van de gegeven naam. Het
   * pairs attribuut is reeds geïnitialiseerd met een lege lijst (ipv null)
   */
  def createAnnotation(name: String): NormalAnnotationExpr = {
    var annotation = new NormalAnnotationExpr
    annotation.setName(new NameExpr(name))
    annotation.setPairs(new ArrayList[MemberValuePair])
    annotation
  }

  /**
   * Maak een annotatie op basis van de gegeven naam. Het
   * pairs attribuut wordt geïnitialiseerd met de opgegeven waarden.
   */
  def createAnnotationWithNameValues(name: String, nameValues: (String, AnyRef)*): NormalAnnotationExpr = {
    var annotation = new NormalAnnotationExpr
    annotation.setName(new NameExpr(name))
    val pairs = new ArrayList[MemberValuePair]
    nameValues.foreach { nameValue =>
      pairs.add(createMemberValuePair(nameValue._1, nameValue._2))
    }
    annotation.setPairs(pairs)
    annotation
  }

  /**
   * Maak een MemberValuePair op basis van gegeven naam en waarde.
   * Afhankelijk van het type waarde wordt de juiste subclass aangemaakt.
   */
  def createMemberValuePair(name: String, value: AnyRef): MemberValuePair = {
    var valueStr = if (value != null) value.toString() else "";
    val expression: Expression =
      if (value.isInstanceOf[Long]) {
        new LongLiteralExpr(valueStr)
      } else if (value.isInstanceOf[Int]) {
        new IntegerLiteralExpr(valueStr)
      } else if (value.isInstanceOf[Boolean]) {
        new BooleanLiteralExpr(value.asInstanceOf[Boolean])
      } else if (classOf[Expression].isAssignableFrom(value.getClass)) {
        value.asInstanceOf[Expression]
      } else {
        new StringLiteralExpr(valueStr)
      }

    new MemberValuePair(name, expression)
  }

  /**
   * Maak een import declaration welke aan een CompilationUnit toegevoegd kan worden
   * d.m.v. de method addImports
   */
  def createImport(packageName: String, isStatic: Boolean, isAsterisk: Boolean): ImportDeclaration =
    new ImportDeclaration(new NameExpr(packageName), isStatic, isAsterisk)
    
  /**
   * Maak een import declaration welke aan een CompilationUnit toegevoegd kan worden
   * d.m.v. de method addImports
   */
  def createImport(packageName: String): ImportDeclaration =
    createImport(packageName, false, false);

  def containsImport(cu: CompilationUnit, importDeclaration: ImportDeclaration) =
    cu.getImports.exists(_.toString == importDeclaration.toString())

  def addImports(cu: CompilationUnit, imports: ImportDeclaration*) {
    if (cu.getImports == null) {
      cu.setImports(new ArrayList[ImportDeclaration])
    }

    imports.foreach { importDeclaration =>
      if (!containsImport(cu, importDeclaration)) {
        cu.getImports().add(importDeclaration);
      }
    }
  }

  /**
   * Removes an import line.
   *
   * @param cu
   * @param className the full class name (including package name)
   * whose import line must be removed.
   */
  def removeImport(cu: CompilationUnit, className: String) {
    val imports = cu.getImports()
    if (imports != null) {
      cu.setImports(nullIfEmpty(imports.filterNot { importDeclaration =>
        className == (importDeclaration.getName.toString + (if (importDeclaration.isAsterisk) ".*" else ""))
      }))
    }
  }

  def addAnnotation(decl: BodyDeclaration, annotation: AnnotationExpr) {
    if (decl.getAnnotations == null) {
      decl.setAnnotations(new ArrayList[AnnotationExpr])
    }
    decl.getAnnotations.add(annotation)
  }

  /**
   * Changes the name of a class or interfaces. Any constructors
   * are renamed as well.
   */
  def setName(decl: ClassOrInterfaceDeclaration, newName: String) {
    decl.setName(newName)

    decl.accept(new VoidVisitorAdapter[Object] {
      override def visit(n: ConstructorDeclaration, arg: Object) {
        n.setName(newName)
        super.visit(n, arg)
      }
    }, null)
  }

  /**
   * Returns the value of a WebLogic annotation in JavaDoc. Any quotes surrounding
   * the value are stripped. Only the first occurrence of the annotation will be
   * returned.
   * <dl>
   *   <dt>input comment line</dt>
   *   <dd><code>* @common:target-namespace namespace="org.migror.ns"</code></dd>
   *   <dt>weblogicAnnotation</dt>
   *   <dd><code>@common:target-namespace</code></dd>
   *   <dt>returns</dt>
   *   <dd><code>org.migror.ns</code></dd>
   * </dl>
   */
  def getJavadocLineWith(decl: BodyDeclaration, weblogicAnnotation: String): Option[String] = {
    if (decl.getJavaDoc != null) {
      decl.getJavaDoc.getContent.split("\r?\n").filter(_.contains(weblogicAnnotation)).foreach { line =>
        val matcher = Pattern.compile(weblogicAnnotation + " .+\\=\"?([^\"]*)").matcher(line)
        if (matcher.find(1)) {
          return Some(matcher.group(1))
        }
      }
    }
    None
  }

  /**
   * Determines if JavaDoc for the given field contains a specific WebLogic annotation.
   */
  def hasJavadocLineWith(decl: FieldDeclaration, weblogicAnnotation: String): Boolean =
    decl.getJavaDoc != null &&
      decl.getJavaDoc.getContent.split("\r?\n").exists(_.contains(weblogicAnnotation))

  /**
   * Removes lines from JavaDoc that contain given annotation. If the remaining JavaDoc
   * is empty (ignoring whitespace and asterisks), it is completely removed.
   */
  def cleanUpJavadoc(decl: BodyDeclaration, weblogicAnnotation: String) {
    if (decl.getJavaDoc != null) {
      val lines = decl.getJavaDoc.getContent.split("\r?\n")
      //val newLines = new ArrayList<String>()
      val elementName = decl.accept(new NameVisitor, null)
			debug(lines.
        filter(_.contains(weblogicAnnotation)).
        map(line => "Removed JavaDoc line '%s' from element %s in class %s".format(line, elementName, decl.getClass.getSimpleName)).mkString("\n"))
      val newLines = lines.filterNot(_.contains(weblogicAnnotation))

      val newJavadoc = newLines.mkString("\r\n")

      if (newLines.mkString("").replace("*", "").trim.isEmpty) {
        decl.setJavaDoc(null)
        debug("Removed JavaDoc from element %s in class %s".format(elementName, decl.getClass.getSimpleName))
      } else {
        decl.getJavaDoc.setContent(newJavadoc)
      }
    }
  }

  private def nullIfEmpty[A <: Any](l: java.util.List[A]): java.util.List[A] =
    if (l == null || l.isEmpty) null else l

}