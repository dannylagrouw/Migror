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
import japa.parser.ast.expr.ClassExpr
import org.migror.internal.Logging
import japa.parser.ast.`type`.{ReferenceType, ClassOrInterfaceType}
import org.migror.javaparser.{NameVisitor, MigrationVisitor}
import japa.parser.ast.body.{ModifierSet, FieldDeclaration, ClassOrInterfaceDeclaration}

class StaticLoggerCorrectionStep extends ParseStep with Logging {
  override val classChanger = Some(new MigrationVisitor {
    private var typeMustChange = false
    private var higherType: Option[ClassOrInterfaceType] = None

    override def visit(decl: ClassOrInterfaceDeclaration, arg: Object) {
      if (higherType.isEmpty) {
        higherType = Some(new ClassOrInterfaceType(decl.getName))
      }
      super.visit(decl, arg)
    }

    override def visit(classExpr: ClassExpr, arg: Object) {
      if (typeMustChange && higherType.isDefined) {
        val oldTypeStr = classExpr.getType.toString
        val newTypeStr = higherType.get.toString

        if (oldTypeStr != newTypeStr) {
          classExpr.setType(new ReferenceType(higherType.get))
          info("Static Logger type changed from %s.class to %s.class".format(oldTypeStr, newTypeStr))
        }
      }
      super.visit(classExpr, arg)
    }

    override def visit(field: FieldDeclaration, arg: Object) {
      typeMustChange = false
      field.getType match {
        case fieldType: ReferenceType =>
          val typeName = fieldType.getType.accept(new NameVisitor, null)
          typeMustChange |= (typeName == "Logger" && (field.getModifiers & ModifierSet.STATIC) > 0)
        case _ =>
      }
      super.visit(field, arg);
    }
  })
}

object StaticLoggerCorrectionStep {
  def apply() = new StaticLoggerCorrectionStep
}