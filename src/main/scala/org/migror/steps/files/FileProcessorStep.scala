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

import org.migror.model.Step

/**
 * Processes a {@link MigrationFile}. This file will be passed by a {@link FilesStep}.
 * Therefore this step must have a FilesStep as its parent.
 */
abstract class FileProcessorStep extends Step {

  var migrationFile: Option[MigrationFile] = None

  override def begin = {
    super.begin
    parent match {
      case Some(step: FilesStep) =>
      case _ => throw new IllegalStateException("This FileProcessorStep of type %s must be executed within a FilesStep.".format(getClass.getSimpleName))
    }
  }
}