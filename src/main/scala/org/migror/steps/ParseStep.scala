package org.migror.steps

import files.{JavaFile, FileProcessorStep}
import japa.parser.ast.CompilationUnit
import japa.parser.ast.visitor.VoidVisitorAdapter
import org.migror.exception.MigrationException

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

abstract class ParseStep extends FileProcessorStep {

  var compilationUnit: CompilationUnit = _

  val classChanger: Option[VoidVisitorAdapter[Object]]

  def executeThisStepOnly {
    parse(migrationFile.get, classChanger)
  }

  override def migrationFile: Option[JavaFile] = super.migrationFile match {
    case Some(f: JavaFile) => Some(f)
    case _ => throw new MigrationException("ParseStep only works with JavaFile")
  }

  def parse(sourceFile: JavaFile, methodChanger: Option[VoidVisitorAdapter[Object]]) {
    try {
      compilationUnit = sourceFile.compilationUnit
      if (methodChanger.isDefined) {
        methodChanger.get.visit(compilationUnit, null)
      }
    } catch {
      case e: Exception => 
    }
  }
}