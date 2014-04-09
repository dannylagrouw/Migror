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

import org.migror.javaparser.MigrationVisitor
import japa.parser.ast.body.{FieldDeclaration, ClassOrInterfaceDeclaration, MethodDeclaration}
import org.migror.steps.ParseStep

class WebLogicAnnotationsCleanupStep extends ParseStep {
  override val classChanger = Some(new MigrationVisitor {
    override def visit(decl: ClassOrInterfaceDeclaration, arg: Object) {
      cleanUpJavadoc(decl, "@editor-info:code-gen")
      cleanUpJavadoc(decl, "@common:target-namespace")
      super.visit(decl, arg)
    }

    override def visit(decl: FieldDeclaration, arg: Object) {
      cleanUpJavadoc(decl, "@common:control")
      super.visit(decl, arg)
    }

    override def visit(decl: MethodDeclaration, arg: Object) {
      cleanUpJavadoc(decl, "@common:operation")
      super.visit(decl, arg)
    }
  })
}

object WebLogicAnnotationsCleanupStep {
  def apply() = new WebLogicAnnotationsCleanupStep
}