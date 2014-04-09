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

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import org.migror.steps.CreateTempDirStep
import org.apache.commons.io.{IOUtils, FileUtils}
import org.migror.model.{Migration, Context}

class FilesWithExtensionStepSpec extends Spec with ShouldMatchers {
  describe("A FilesWithExtensionStep") {
    Context.put(Context.SOURCE_PATH, "/Users/danny/Documents/Projecten/Migror/src/test/resources/org/migror/test/source")
    Migration(
      CreateTempDirStep("FilesWithExtensionStep", Context.TARGET_PATH),
      FilesWithExtensionStep("FilesWithExtensionStep", "txt")
    ).execute
  }

}