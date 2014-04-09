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
package org.migror.steps.replace

import org.migror.model.rules.RuleSet
import japa.parser.ast.CompilationUnit
import java.io.ByteArrayInputStream
import japa.parser.JavaParser
import org.migror.javaparser.MigrationVisitor
import org.migror.internal.LangUtils.withDo


class RuleSetReplacers(val ruleSet: RuleSet) extends Replacers(replacers = ruleSet.replacers) {
  override def findAndReplace(fileName: String, source: String): String = {
    var result = super.findAndReplace(fileName, source)
    if (acceptsFile(fileName))
      addImports(result)
    else
      result
  }

  override def acceptsFile(fileName: String): Boolean =
    ruleSet.fileFilter.isEmpty || fileName.matches(ruleSet.fileFilter.get)

  def addImports(source: String): String =
    withDo(JavaParser.parse(new ByteArrayInputStream(source.getBytes))) { cu =>
      cu.accept(new MigrationVisitor {
        override def visit(n: CompilationUnit, arg: Object) {
          ruleSet.imports.foreach(importStr => addImports(n, createImport(importStr)))
          super.visit(n, arg)
        }
      }, null)

      cu.toString
    }

}