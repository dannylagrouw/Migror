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
import org.migror.model.Context

class FilesStepSpec extends Spec with ShouldMatchers {
  CreateTempDirStep("filesstepspec-source", "filesstepspec-source").execute
  CreateTempDirStep("filesstepspec-target", "target.location").execute
//  CreateTempDirStep("filesstepspec-source", "filesstepspec-source").deleteOnExit.execute
//  CreateTempDirStep("filesstepspec-target", "target.location").deleteOnExit.execute
  describe("A FilesStep") {
    it("should copy files from source to target") {
      MockFilesStep("filesstepspec-source").execute
      println(Context.dump)
    }
  }

}