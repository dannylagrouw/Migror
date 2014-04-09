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
package org.migror.steps

import java.io.File
import org.migror.internal.Logging
import org.migror.model.{Context, Step}
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.{AbstractFileFilter, TrueFileFilter}
import scala.collection.JavaConversions._

class FileDeleteStep(startDir: String)(fileFilter: File => Boolean) extends Step with Logging {
  def executeThisStepOnly {
    if (steps.isEmpty) {
      val absoluteFile = new File(Context.targetPath, startDir)
      val legacyFilter = new AbstractFileFilter {
        override def accept(file: File) = fileFilter(file)
      }
      val files: List[File] = FileUtils.listFiles(absoluteFile, legacyFilter, TrueFileFilter.INSTANCE).asInstanceOf[java.util.Collection[File]].toList
      files.foreach(deleteFile)
    }
  }

  def deleteFile(file: File) {
    if (file.exists) {
      if (file.isDirectory) {
        FileUtils.deleteDirectory(file)
      } else {
        file.delete
      }
      info("Deleted %s %s".format(
        if (file.isDirectory) "Directory" else "File",
        file.getAbsolutePath))
    }
  }

  def deleteFromContext(file: File) {
    Context.removeValue(file)
  }
}

object FileDeleteStep {
  def apply(startDir: String)(fileFilter: File => Boolean) =
    new FileDeleteStep(startDir)(fileFilter)
}