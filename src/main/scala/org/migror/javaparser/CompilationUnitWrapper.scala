/*
 *  Copyright 2010 Danny Lagrouw
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

import japa.parser.ast.CompilationUnit
import collection.JavaConversions._
import japa.parser.ast.body.{TypeDeclaration, ModifierSet}
import japa.parser.ParseException

class CompilationUnitWrapper(cu: CompilationUnit) {

  def firstClassName = {
    cu.getTypes.find(decl => ModifierSet.isPublic(decl.getModifiers)) match {
      case Some(decl) => decl.getName
      case None => throw new ParseException("Unable to find a public class in source " + cu.toString)
    }
  }

}

object CompilationUnitWrapper {

  implicit def compilationUnitWrapper(cu: CompilationUnit) = new CompilationUnitWrapper(cu)

}