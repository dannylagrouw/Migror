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

/**
 * A file that must be migrated. Besides location information, objects of
 * this class also hold the file contents itself, to be manipulated during
 * migration processing steps.
 * @param sourcePath start path from which files are being migrated. If this
 * file is located in a child folder beneath this start path, it will be
 * migrated to the same relative folder under targetPath.
 * @param sourceFile the file to be migrated.
 * @param targetPath the start path to which files are being migrated.
 */
class MigrationFile(sourcePath: String, sourceFile: File, targetPath: File) {

  var _targetFile: File = _
  var _contents = FileUtils.readFileToString(sourceFile)

  /**
   * Schrijft het bestand weg naar {@link #getDoelFile()}.
   */
  def writeTargetFile() {
    if (!targetFile.getParentFile.exists) {
      targetFile.getParentFile.mkdirs
    }
    FileUtils.writeStringToFile(targetFile, contents)
  }

  /**
   * Geeft de volledige bestandsnaam terug waarheen het gemigreerde bestand
   * zal worden weggeschreven.
   *
   * @return de ingestelde doelbestandsnaam, of, als geen bestandsnaam is
   *         ingesteld, de naam van het bronbestand in {@link #getDoelpad()}.
   */
  def targetFile: File =
    if (_targetFile == null)
      new File(targetPath, sourceFile.getAbsolutePath.drop(sourcePath.length))
    else
      _targetFile

  /**
   * Stelt de volledige bestandsnaam in waarheen het gemigreerde bestand
   * weggeschreven moet worden.
   *
   * @param doelFile
   */
  def targetFile_=(f: File) {
    _targetFile = f
  }

  def contents = _contents

  def contents_=(s: String) {
    _contents = s
  }
}

object MigrationFile {

  def apply(sourcePath: String, sourceFile: File, targetPath: File) = {
    new MigrationFile(sourcePath, sourceFile, targetPath)
  }
}