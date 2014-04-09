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

import org.migror.steps.files.FileProcessorStep

class ReplaceStep(val replacers: Replacers) extends FileProcessorStep {

  def this(replacer: Replacer) = this(Replacers(replacer))

  def executeThisStepOnly {
    migrationFile match {
      case Some(file) =>
        file.contents = replacers.findAndReplace(file.sourceFile.getAbsolutePath, file.contents)
      case _ =>
    }
  }
}

object ReplaceStep {
  def apply(replacers: Replacers) = new ReplaceStep(replacers)
}