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
import org.apache.commons.io.FileUtils
import org.migror.model.Context

class MockFilesStep(sourcePath: String, prefix: String) extends FilesStep(sourcePath, false) {

  val sourceFilenames = List(prefix + "-file1", "path1/" + prefix + "-file2")

  val getSourceFiles = sourceFilenames.map { name =>
    createTempFile(name, name + " contents")
  }

  def createTempFile(name: String, contents: String): File = {
    val f = new File(sourcePath, name)
    FileUtils.writeStringToFile(f, contents)
    f
  }

}

object MockFilesStep {
  def apply(sourcePathContextKey: String) = new MockFilesStep(Context.getFile(sourcePathContextKey).getAbsolutePath, sourcePathContextKey)
}