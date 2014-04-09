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
package org.migror.steps

import org.migror.model.Context
import japa.parser.ast.`type`.ClassOrInterfaceType
import japa.parser.ast.expr.{NameExpr, MethodCallExpr}
import scala.collection.JavaConversions._
import japa.parser.ast.{PackageDeclaration, CompilationUnit}
import org.migror.javaparser.{NameVisitor, MigrationVisitor}

class PackageRenameStep(sourcePackage: String, targetPackage: String, classesToInclude: List[String], classesToExclude: List[String]) extends ParseStep {

  val NotAllowed = List("Long", "Boolean", "String", "Integer", "BigDecimalHelper", "BigDecimal")

  private def isValidClassName(className: String): Boolean = {
    if (!Character.isUpperCase(className.charAt(0)) || NotAllowed.contains(className)) {
      false
    } else {
      classesToInclude.exists(className.matches) && !classesToExclude.exists(className.matches)
    }
  }

  val classChanger = Some(new MigrationVisitor {
    override def visit(cu: CompilationUnit, arg: Object) {
      if (cu.getImports != null) {
        correctImports(cu)
      }
      if (cu.getPackage.getName.toString == sourcePackage) {
        info("Renaming: package '%s' becomes '%s'".format(cu.getPackage.getName.toString, targetPackage))
        cu.setPackage(new PackageDeclaration(new NameExpr(targetPackage)))
      }
      super.visit(cu, arg)
    }

    override def visit(n: ClassOrInterfaceType, arg: Object) {
      checkType(n)
      super.visit(n, arg)
    }

    override def visit(n: MethodCallExpr, arg: Object) {
				// Calls to valueOf methods may reference classes that are to be renamed.
				if (n.getName == "valueOf" && n.getScope.isInstanceOf[NameExpr]) {
					val className = n.getScope.toString
					if (isValidClassName(className)) {
						checkType(new ClassOrInterfaceType(className))
					}
				}
				super.visit(n, arg)
    }

    private def correctImports(cu: CompilationUnit) {
      cu.getImports.foreach { imp =>
        val importedPackageAndClass = imp.getName.toString
        val index = importedPackageAndClass.lastIndexOf(".")
        val packageName = importedPackageAndClass.substring(0, index)
        val className = importedPackageAndClass.substring(index + 1)

        if (packageName == sourcePackage && isValidClassName(className)) {
          val newImport = targetPackage + "." + className
          info("Refactoring: import '%s' becomes '%s'".format(importedPackageAndClass, newImport))
          imp.setName(new NameExpr(newImport))
        }
      }
    }

    private def checkType(classType: ClassOrInterfaceType) {
      val currentPackage = compilationUnit.getPackage.getName.toString
      val className = classType.getName

      if (isValidClassName(className)) {
        if (classType.getScope == null && sourcePackage == currentPackage) {
          val isImported =
            if (compilationUnit.getImports != null) {
              compilationUnit.getImports.exists { imp =>
                imp.getName.toString.substring(imp.getName.toString.lastIndexOf(".") + 1) == className
              }
            } else {
              false
            }

          if ((compilationUnit.getImports == null || !isImported)) {
            val newImport = targetPackage + "." + className
            info("Refactoring: import '%s' added".format(newImport))
            addImports(compilationUnit, createImport(newImport))
          }
        } else if (classType.getScope != null && classType.getScope.toString == sourcePackage) {
          classType.setScope(null)
          info("Refactoring: package of class '%s' changed from '%s' into '%s' and added to imports".format(
              className, sourcePackage, targetPackage))

          addImports(compilationUnit, createImport(targetPackage + "." + className))
        }
      }
    }

  })
}

object PackageRenameStep {
  def apply(sourcePackage: String, targetPackage: String, classesToInclude: List[String], classesToExclude: List[String]) =
    new PackageRenameStep(sourcePackage, targetPackage, classesToInclude, classesToExclude)
}
