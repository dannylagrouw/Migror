package org.migror.steps.weblogic

import org.migror.steps.ParseStep
import org.migror.javaparser.MigrationVisitor
import japa.parser.ast.CompilationUnit
import japa.parser.ast.body.FieldDeclaration
import japa.parser.ast.expr.NormalAnnotationExpr

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

/**
 * Adds @EJB annotation to fields that are references to WebLogic controls.
 */
class WebLogicControlReferenceStep extends ParseStep {
  override val classChanger = Some(new MigrationVisitor {
    override def visit(cu: CompilationUnit, arg: Object) {
      addImports(cu, createImport("javax.ejb.EJB"))
      super.visit(cu, arg)
    }

    override def visit(field: FieldDeclaration, arg: Object) {
      if (field.getJavaDoc != null && field.getJavaDoc.getContent.contains("@common:control")) {
          // Assign annotations to field
        addAnnotation(field, createAnnotation("EJB"))
      }
      super.visit(field, arg);
    }
  })
}

object WebLogicControlReferenceStep {
  def apply() = new WebLogicControlReferenceStep
}