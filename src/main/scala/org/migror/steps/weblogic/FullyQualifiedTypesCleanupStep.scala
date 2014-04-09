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
import japa.parser.ast.`type`.ClassOrInterfaceType


class FullyQualifiedTypesCleanupStep extends ParseStep {
  val classChanger = Some(new MigrationVisitor {
    override def visit(n: ClassOrInterfaceType, arg: Object) {
      if (n.getScope != null && n.getScope.toString == "java.lang") {
        n.setScope(null)
      }
    }
  })
}

object FullyQualifiedTypesCleanupStep {
  def apply() = new FullyQualifiedTypesCleanupStep
}