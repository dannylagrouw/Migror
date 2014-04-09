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
import java.io.File

class FilesStepSpec extends Spec with ShouldMatchers {
  CreateTempDirStep("filesstepspec-source", "filesstepspec-source").deleteOnExit.execute
  CreateTempDirStep("filesstepspec-readonly-source", "filesstepspec-readonly-source").deleteOnExit.execute
  CreateTempDirStep("filesstepspec-target", "target.location").deleteOnExit.execute
  describe("A FilesStep") {
    val step = MockFilesStep("filesstepspec-source")
    describe("when executed") {
      step.execute
      it("should copy files from source to target") {
        step.sourceFilenames.foreach { name =>
          (new File(Context.getFile("target.location"), name).exists) should be (true)
        }
      }
    }
    describe("when asked for fullTargetPath") {
      it("should return the Context's target location") {
        (step.fullTargetPath) should be (Context.getFile("target.location"))
      }
    }
    describe("when explicitly passed a target path") {
      it("should return the Context's target location plus the target path") {
        step.targetPath = Some("another/target/path")
        (step.fullTargetPath) should be (new File(Context.getFile("target.location"), "another/target/path"))
      }
    }
    describe("when readonly set") {
      val step2 = MockFilesStep("filesstepspec-readonly-source")
      step2.setReadOnly(true)
      it("should not write files to the target location") {
        step2.execute
        step2.sourceFilenames.foreach { name =>
          (new File(Context.getFile("target.location"), name).exists) should be (false)
        }
      }
    }
  }

}