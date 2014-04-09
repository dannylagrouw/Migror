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

import org.migror.steps.ParseStep
import org.migror.javaparser.MigrationVisitor
import japa.parser.ast.body.ClassOrInterfaceDeclaration

class WebLogicControlInterfaceStep extends ParseStep {
  val classChanger = Some(new MigrationVisitor {
    override def visit(clazz: ClassOrInterfaceDeclaration, arg: Object) {
      if (clazz.isInterface) {
        removeExtends(clazz, "Control")
        removeImport(compilationUnit, "com.bea.control.Control")
      }
      super.visit(clazz, arg)
    }
  })
  
}

object WebLogicControlInterfaceStep {
  def apply() = new WebLogicControlInterfaceStep
}