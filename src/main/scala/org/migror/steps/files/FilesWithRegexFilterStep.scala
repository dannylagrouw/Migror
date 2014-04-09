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
import java.util.regex.Pattern
import org.apache.commons.io.filefilter.{AbstractFileFilter, FileFilterUtils}
import java.io.File

/**
 * Specific type of {@link FilesStep}, that loops recursively over files with
 * names matching a regular expression.
 *
 * @param sourcePath
 *            the path in which files will be found recursively.
 * @param isRelativePath
 *            indicates whether sourcePath is a relative path to
 *            {@link Context#sourcePath}.
 * @param regexFilter
 *            one or more regular expressions, one of which must match to
 *            include a file.
 */
class FilesWithRegexFilterStep(sourcePath: String, isRelativePath: Boolean, regexFilters: String*) extends FilesStep(sourcePath, isRelativePath) {

  def this(sourcePath: String, regexFilters: String*) = this(sourcePath, true, regexFilters:_*)

  val regexes = regexFilters.toList.map(Pattern.compile)

  def getSourceFiles = {
    val fileFilter = new AbstractFileFilter {
        override def accept(dir: File, name: String) =
          regexes.exists(_.matcher(name).matches)
    }
    FileUtils.listFiles(fullSourcePath.getAbsoluteFile, fileFilter, FileFilterUtils.trueFileFilter).asInstanceOf[Collection[File]]
  }
}

object FilesWithRegexFilterStep {
  def apply(sourcePath: String, isRelativePath: Boolean, regexFilters: String*) = new FilesWithRegexFilterStep(sourcePath, isRelativePath, regexFilters:_*)
  def apply(sourcePath: String, regexFilters: String*) = new FilesWithRegexFilterStep(sourcePath, regexFilters:_*)
}
