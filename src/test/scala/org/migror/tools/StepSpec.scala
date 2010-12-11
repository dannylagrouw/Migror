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
package org.migror.tools

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import org.migror.model.{Context, MockStep, Step}

class StepSpec extends Spec with ShouldMatchers {
  describe("Step.skip") {
    describe("when skip property not set") {
      it("should return false") {
        val step = new MockStep
        (step.skip) should be (false)
      }
    }
    describe("when skip property set") {
      it("should return true") {
        val step = new MockStep
        Context.put("MockStep.skip", "true")
        (step.skip) should be (true)
        Context.clear
      }
    }
  }

  describe("Step.execute") {
    describe("when skip property set") {
      it("should not execute") {
        val step = new MockStep
        Context.put("MockStep.skip", "true")
        step.execute
        (step.executed) should be (0)
        Context.clear
      }
      it("should not have called begin()") {
        val step = new MockStep
        Context.put("MockStep.skip", "true")
        step.execute
        (step.begun) should be (0)
        Context.clear
      }
    }
    describe("when no children") {
      it("should execute just the step") {
        val step = new MockStep
        step.execute
        (step.executed) should be (1)
      }
      it("should have called begin()") {
        val step = new MockStep
        step.execute
        (step.begun) should be (1)
      }
    }
    describe("when having child steps") {
      it("should execute them and then itself") {
        val step = new MockStep
        val child1 = new MockStep
        val child2 = new MockStep
        step.add(child1)
        step.add(child2)
        step.execute
        (step.executed) should be (1)
        (child1.executed) should be (1)
        (child2.executed) should be (1)
        (step.begun) should be (1)
        (child1.begun) should be (1)
        (child2.begun) should be (1)
      }
    }
  }

}