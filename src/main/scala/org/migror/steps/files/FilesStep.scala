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
 * loaded, processed by any child steps, and finally saved to the target
 * location.
 */
abstract class FilesStep(sourcePath: String, isRelativePath: Boolean) extends Step {

  var targetPath: Option[String] = None

  var filters = List.empty[FileFilter]

  var readOnly = false

  def this(sourcePath: String) = this(sourcePath, true)

  /**
   * Returns a collection of files to be processed in this step.
   */
  def getSourceFiles: Iterable[File]

  def executeThisStepOnly = {}

  /**
   * Returns the target path for this step's source files, prepended
   * by the Context's target location.
   */
  def fullTargetPath =
    targetPath match {
      case None => Context.targetPath
      case Some(p) => new File(Context.targetPath, p)
    }

  def fullSourcePath =
    if (isRelativePath)
      new File(Context.sourcePath, sourcePath)
    else
      new File(Context.replaceVars(sourcePath))

  /**
   * Execution of this step entails looping over all source files,
   * passing them to all child steps for processing, and saving the
   * processed files to the target location.
   */
  override def execute = {
    getSourceFiles.foreach { sourceFile =>
      val migrationFile = MigrationFile(fullSourcePath.getAbsolutePath, sourceFile, fullTargetPath)
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
      } else {
        info("Skipping " + migrationFile)
      }
    }
  }

  /**
   * Adds a file filter to determine which files will be included for
   * processing. A file is included if it matches at least one of the
   * file filters, or if no filters are present.
   */
  def add(filter: FileFilter) = {
    filters ::= filter
    this
  }

  /**
   * Sets the target path where files will be saved after processing.
   */
  def setTargetPath(path: String) = {
    targetPath = Some(path)
    this
  }

  /**
   * Determines if the migration file must be included for processing.
   */
  def include(migrationFile: MigrationFile) =
    filters.isEmpty || filters.exists(_.matches(migrationFile))

  /**
   * Marks this files step as readonly, meaning processed files will
   * not be saved to the target location.
   */
  def setReadOnly(readOnly: Boolean) = {
    this.readOnly = readOnly
    this
  }
}