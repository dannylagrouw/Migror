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
package org.migror.steps.files

import java.io.File
import org.migror.model.{Context, Step}

/**
 * Iterates over a collection of files that must be migrated. Each file is
 * loaded, processed by any child steps, and finally saved at the target
 * location.
 */
abstract class FilesStep(sourcePath: String) extends Step {

  var targetPath: Option[String] = None

  var filters = List.empty[FileFilter]

  var readOnly = false

  /**
   * Returns a collection of files to be processed in this step.
   */
  def getSourceFiles: Collection[File]

  def executeThisStepOnly = {}

  /**
   * Returns the target path for this step's source files, prepended
   * by the Context's target location.
   */
  def fullTargetPath =
    targetPath match {
      case None => Context.getFile("target.location")
      case Some(p) => new File(Context.getFile("target.location"), p)
    }

  override def execute = {
    getSourceFiles.foreach { sourceFile =>
      val migrationFile = MigrationFile(sourcePath, sourceFile, fullTargetPath)
      if (include(migrationFile)) {
        info("MigrationFile " + migrationFile)
        steps.foreach { step =>
          step match {
            case filesStep: FileProcessorStep => filesStep.migrationFile = Some(migrationFile)
          }
          step.execute
        }
        if (!readOnly) {
          migrationFile.writeTargetFile
        }
      }
    }
  }

  /**
   * Determines if the migration file must be included for processing.
   */
  def include(migrationFile: MigrationFile) =
    filters.isEmpty || filters.exists(_.matches(migrationFile))
}