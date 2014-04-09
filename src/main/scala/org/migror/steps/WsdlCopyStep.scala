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

import files.FileProcessorStep
import java.io.File
import org.migror.model.Context


/**
 * Copies a source file (containing a WSDL) to a specific target location.
 * The target location will be deduced from the <code>targetNamespace</code>
 * attribute in the WSDL.
 * The WSDL will also be registered in Context so that PomStep may include
 * it when generating a pom.xml.
 */
class WsdlCopyStep(relativeTargetDirName: String) extends FileProcessorStep {
  val targetDirectory = new File(Context.targetPath, relativeTargetDirName);

  def executeThisStepOnly {
    val targetLocationName = WsdlHelper.bepalenDirectory(migrationFile.get)
    val targetLocationFile = findTargetFile(targetLocationName)
    info("Copying WSDL %s to directory %s".format(targetLocationFile.getName, targetLocationName))
    migrationFile.get.targetFile = targetLocationFile
  }

  private def findTargetFile(targetLocationName: String) =
    new File(findTargetLocation(targetLocationName), migrationFile.get.sourceFile.getName)

  private def findTargetLocation(targetLocationName: String) =
    new File(targetDirectory.getAbsolutePath, targetLocationName)

}