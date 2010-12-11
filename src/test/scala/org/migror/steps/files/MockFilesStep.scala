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

class MockFilesStep(sourcePath: String) extends FilesStep(sourcePath) {

  val getSourceFiles = List(
    createTempFile("file1", "file 1 contents"),
    createTempFile("path1/file2", "file 2 contents")
  )

  def createTempFile(name: String, contents: String): File = {
    val f = new File(sourcePath, name)
    FileUtils.writeStringToFile(f, contents)
    f
  }

}

object MockFilesStep {
  def apply(sourcePath: String) = new MockFilesStep(Context.getFile(sourcePath).getAbsolutePath)
}