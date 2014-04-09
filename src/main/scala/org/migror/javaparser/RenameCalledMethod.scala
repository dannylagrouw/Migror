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

import japa.parser.ast.expr.{Expression, MethodCallExpr}

/**
 * Changes a method name in the location where the method is being called.
 * For example:
 * <ul>
 * <li>fieldName = "person"</li>
 * <li>searchMethodName = "getName"</li>
 * <li>replaceMethodName = "getNumber"</li>
 * </ul>
 * <p>
 * This changes a call <code>person.getName()</code> to
 * <code>person.getNumber()</code>.
 * <p>
 * If fieldName is None then only isolated calls (not on an object) will be replaced.
 * For example <code>getName()</code> becomes <code>getNumber()</code>.
 * <p>
 * Usage:
 * <code>RenameCalledMethod("person", "getName", "getNumber").visit(compilationUnit, null)</code>
 * <br/>
 * <code>RenameCalledMethod("getName", "getNumber").visit(compilationUnit, null)</code>
 */
class RenameCalledMethod(fieldName: Option[String], searchMethodName: String, replaceMethodName: String) extends MigrationVisitor {

  override def visit(decl: MethodCallExpr, arg: Object) {
      super.visit(decl, arg)
      migrateJwsContext(decl)
  }

  private def migrateJwsContext(decl: MethodCallExpr) {
    if (fieldNameMatches(decl.getScope) && searchMethodName == decl.getName) {
      decl.setName(replaceMethodName)
    }
  }

  private def fieldNameMatches(scope: Expression) =
    (scope == null && fieldName.isEmpty) || (scope != null && fieldName.get == scope.toString)

}

object RenameCalledMethod {
  def apply(fieldName: String, searchMethodName: String, replaceMethodName: String) =
    new RenameCalledMethod(Some(fieldName), searchMethodName, replaceMethodName)
  def apply(searchMethodName: String, replaceMethodName: String) =
    new RenameCalledMethod(None, searchMethodName, replaceMethodName)
}
