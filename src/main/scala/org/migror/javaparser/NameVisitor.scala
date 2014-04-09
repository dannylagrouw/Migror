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

import japa.parser.ast.visitor.GenericVisitorAdapter
import japa.parser.ast.`type`.ClassOrInterfaceType
import japa.parser.ast.body.{MethodDeclaration, FieldDeclaration, ClassOrInterfaceDeclaration}

/**
 * Returns an element's name.
 */
class NameVisitor extends GenericVisitorAdapter[String, Object] {
  override def visit(decl: ClassOrInterfaceDeclaration, arg: Object): String =
    decl.getName

  override def visit(decl: ClassOrInterfaceType, arg: Object): String =
    decl.getName

  override def visit(field: FieldDeclaration, arg: Object): String =
    field.getVariables.get(0).getId.getName

  override def visit(method: MethodDeclaration, arg: Object): String =
    method.getName
}