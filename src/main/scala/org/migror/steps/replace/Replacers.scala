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

import org.migror.internal.Logging
import org.apache.commons.io.FileUtils
import java.io.File
import tools.nsc.{Settings, Interpreter}
import org.migror.model.rules.{RuleSet, MigrationRules}


/**
 * Collection of Replacers, that each know how to find and replace
 * text fragments in a source text. A collection of replacers
 * may contain child collections, that in turn contain replacers.
 */
class Replacers(val replacers: List[Replacer] = List.empty[Replacer],
                val childReplacers: List[Replacers] = List.empty[Replacers]
                ) extends Logging {

  def findAndReplace(fileName: String, source: String): String = {
    var result = source
    if (acceptsFile(fileName)) {
      result = childReplacers.filter(_.acceptsFile(fileName)).foldLeft(result) { (tempResult, childReplacer) =>
        childReplacer.findAndReplace(fileName, tempResult)
      }
      result = replacers.foldLeft(result) { (tempResult, replacer) =>
        replacer.findAndReplace(tempResult)
      }
    }
    result
  }

  def acceptsFile(fileName: String): Boolean = true
}

class MigrationRulesHolder {
  var migrationRules: MigrationRules = _
}

object Replacers {
  def apply(replacers: Replacer*) = new Replacers(replacers.toList)

  def apply(migrationRules: MigrationRules) = {
    new Replacers(childReplacers = migrationRules.ruleSets.toList.map { ruleSet =>
      new Replacers(ruleSet.replacers)
    })
  }

  def apply(replacersConfigFileName: File) {
    val holder = new MigrationRulesHolder
    val replacersConfigScript = FileUtils.readFileToString(replacersConfigFileName)
    val settings = new Settings
    settings.usejavacp.value = true
    val interpreter = new Interpreter(settings)
    interpreter.bind("$holder", holder.getClass.getCanonicalName, holder)
    val result = interpreter.interpretExpr(replacersConfigScript + "; $holder.migrationRules = rules")
    println("result = " + result)
    println("MigrationRules = " + holder.migrationRules)
  }
}
