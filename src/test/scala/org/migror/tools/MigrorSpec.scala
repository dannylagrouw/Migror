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
import org.migror.model.{MockStep, Step, Migration}

class MockMigration extends Migration {
  val steps = List(new MockStep)
}

class MockFailingMigration extends Migration {
  val steps = List.empty[MockStep]

  override def execute: Unit = {
    throw new RuntimeException
  }
}

class MigrorSpec extends Spec with ShouldMatchers {
  describe("A Migror") {
    it("should accept a Migration") {
      new Migror(new MockMigration) should not be (null)
    }
    describe("when passed a Migration") {
      val migration = new MockMigration
      it("should execute all steps in that Migration") {
        new Migror(migration).execute
        (migration.steps(0).executed) should be (1)
      }
    }
    describe("when started from the command line") {
      val returnCode = Migror.startFromCommandLine(Array(classOf[MockMigration].getName))
      it("should return code 0") {
        returnCode should be (0)
      }
    }
    describe("when started from the command line with an invalid class name") {
      val returnCode = Migror.startFromCommandLine(Array("does.not.Exist"))
      it("should return code 1") {
        returnCode should be (1)
      }
    }
    describe("when started from the command line and the Migration fails") {
      val returnCode = Migror.startFromCommandLine(Array(classOf[MockFailingMigration].getName))
      it("should return code 1") {
        returnCode should be (1)
      }
    }
  }

}