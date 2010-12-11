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

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import org.migror.model.Migration

class CreateTempDirStepSpec extends Spec with ShouldMatchers {
  describe("A CreateTempDirStepSpec") {
    it("should create a temp dir") {
      val step = CreateTempDirStep("createtempdirstepspec1-", "temp.dir")
      Migration(step).execute
      (step.tempDir) should not be ('empty)
      (step.tempDir.get.exists) should be (true)
      (step.tempDir.get.isDirectory) should be (true)
    }
    describe("when deleteOnExit called") {
      it("should remove the temp dir on exit") {
        val step = CreateTempDirStep("createtempdirstepspec2-", "temp.dir").deleteOnExit
        Migration(step).execute
        step.deleteTempDir
        (step.tempDir) should not be ('empty)
        (step.tempDir.get.exists) should be (false)
      }
    }
  }

}