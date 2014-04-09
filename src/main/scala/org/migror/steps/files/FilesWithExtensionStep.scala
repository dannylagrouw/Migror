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

import org.apache.commons.io.FileUtils

import collection.JavaConversions._
import java.io.File

/**
 * Specific type of {@link FilesStep}, that loops recursively over files with
 * certain extensions.
 *
 * @param sourcePath
 *            the path in which files will be found recursively.
 * @param isRelativePath
 *            indicates whether sourcePath is a relative path to
 *            {@link Context#sourcePath}.
 * @param extensions
 *            one or more extensions, one of which must match to
 *            include a file.
 */
class FilesWithExtensionStep(sourcePath: String, isRelativePath: Boolean, extensions: String*) extends FilesStep(sourcePath, isRelativePath) {

  def this(sourcePath: String, extensions: String*) = this(sourcePath, true, extensions:_*)

  def getSourceFiles: Iterable[File] = {
    info("Getting source files from " + fullSourcePath)
    asScalaIterable(FileUtils.listFiles(fullSourcePath.getAbsoluteFile, if (extensions.length == 0) null else extensions.toArray, true).asInstanceOf[java.util.Collection[File]])
  }
}

object FilesWithExtensionStep {
  def apply(sourcePath: String, isRelativePath: Boolean, extensions: String*) = new FilesWithExtensionStep(sourcePath, isRelativePath, extensions:_*)
  def apply(sourcePath: String, extensions: String*) = new FilesWithExtensionStep(sourcePath, extensions:_*)
}