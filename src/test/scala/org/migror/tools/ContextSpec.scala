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
import java.io.File
import org.migror.model.{ContextTypeException, Context}

class ContextSpec extends Spec with ShouldMatchers {

  val FILE1 = new File("/tmp/ContextSpec1.tmp")
  val FILE2 = "/tmp/ContextSpec2.tmp"

  describe("Context.getString(key)") {
    loadContext
    it("should return a String property value") {
      (Context.getString("prop1")) should be (Some("value1"))
    }
    it("should return None if key not present") {
      (Context.getString("notthere")) should be (None)
    }
  }
  describe("Context.getString(key, defaultValue)") {
    loadContext
    it("should return a String property value") {
      (Context.getString("prop1", "default")) should be ("value1")
    }
    it("should return a default String value") {
      (Context.getString("notthere", "default")) should be ("default")
    }
  }
  describe("Context.contains") {
    loadContext
    it("should return true if key found") {
      (Context.contains ("prop1")) should be (true)
    }
    it("should return false if key not found") {
      (Context.contains("notthere")) should be (false)
    }
  }
  describe("Context.getList") {
    loadContext
    describe("first time") {
      it("should return an empty list") {
        (Context.getList("emptylist")) should be ('empty)
      }
    }
    describe("when values are added") {
      Context.add("prop2", "value2")
      Context.add("prop2", "value3")
      it("should return those values") {
        (Context.getList("prop2")) should be (List("value2", "value3"))
      }
    }
    describe("when asked for the wrong type") {
      it("should produce an error") {
        evaluating { Context.getList("prop1") } should produce [ContextTypeException]
      }
    }
  }
  describe("Context.add") {
    loadContext
    describe("when adding a non-unique value") {
      Context.add("add-non-unique", "value")
      Context.add("add-non-unique", "value")
      it("should add that value") {
        (Context.getList("add-non-unique")) should be (List("value", "value"))
      }
    }
  }
  describe("Context.addUnique") {
    loadContext
    describe("when adding a unique value") {
      Context.addUnique("add-unique1", "value")
      it("should add that value") {
        (Context.getList("add-unique1")) should be (List("value"))
      }
    }
    describe("when adding a non-unique value") {
      Context.addUnique("add-unique2", "value")
      Context.addUnique("add-unique2", "value")
      it("should not add that value") {
        (Context.getList("add-unique2")) should be (List("value"))
      }
    }
    describe("when adding another unique value") {
      Context.addUnique("add-unique3", "value")
      Context.addUnique("add-unique3", "value")
      Context.addUnique("add-unique3", "value2")
      it("should not add that value") {
        (Context.getList("add-unique3")) should be (List("value", "value2"))
      }
    }
  }
  describe("Context.getFile") {
    loadContext
    describe("when asked for a file") {
      it("should return that file") {
        (Context.getFile("file1")) should be (FILE1)
      }
    }
    describe("when asked for a file name") {
      it("should return that file") {
        (Context.getFile("file2")) should be (new File(FILE2))
      }
    }
    describe("when file property not present") {
      it("should throw an exception") {
        evaluating { Context.getFile("notthere") } should produce [ContextTypeException]
      }
    }
  }
  describe("Context.dump") {
    loadContext
    println(Context.replaceVars("prop1 = ${prop1} and file1 = ${file1}."))
  }

  def loadContext {
    Context.put("prop1", "value1")
    Context.put("file1", FILE1)
    Context.put("file2", FILE2)
  }
}