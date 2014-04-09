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
package org.migror.steps

import java.io.{IOException, File}
import org.apache.commons.io.FileUtils
import org.migror.internal.LangUtils
import org.migror.model.{Context, Step}

/**
 * Creates a temporary directory and stores its path name in the
 * {@link Context} under the specified key.
 *
 * @param prefix
 *            prefix for the temporary directory name.
 * @param contextKey
 *            the path name to the temporary directory must be
 *            stored under this key in the Context.
 */
class CreateTempDirStep(prefix: String, contextKey: String) extends Step {

  var tempDir: Option[File] = None

  def executeThisStepOnly = {
    tempDir = Some(createTempDirectory)
    Context.put(contextKey, tempDir.get);
    info("Temp dir %s created under key %s".format(tempDir.get, contextKey))
  }

  private def createTempDirectory: File = {
      val temp = File.createTempFile(prefix, System.nanoTime.toString)
      if (!temp.delete) {
          throw new IOException("Could not delete temp file: " + temp.getAbsolutePath)
      }
      if (!temp.mkdir) {
          throw new IOException("Could not create temp directory: " + temp.getAbsolutePath);
      }
      temp
  }

  /**
   * Configures the temporary directory to be automatically removed at the end of
   * this Migror session.
   */
  def deleteOnExit: CreateTempDirStep = {
    LangUtils.addShutdownHook {
      deleteTempDir
    }
    this
  }

  def deleteTempDir {
    if (tempDir.isDefined) {
      debug("Verwijderen tijdelijke directory %s".format(tempDir.get))
      FileUtils.deleteQuietly(tempDir.get)
    }
  }
}

object CreateTempDirStep {
  def apply(prefix: String, contextKey: String) = new CreateTempDirStep(prefix, contextKey)
}