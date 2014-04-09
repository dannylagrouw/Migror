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
package org.migror.sample

import org.migror.steps.CreateTempDirStep
import org.migror.model.{Context, Migration}
import org.migror.tools.Migror
import org.migror.steps.files.FilesWithExtensionStep
import org.migror.steps.files.FilesWithExtensionStep._
import org.migror.steps.weblogic._
import java.io.File
import org.migror.steps.replace.Replacers

class SampleMigration extends Migration {
  println("Path: " + (new java.io.File(".")))
  def steps = List(
    // create a temp dir where migrated files will end up
    CreateTempDirStep("migror-sample-migration", Context.TARGET_PATH)
    ,
    FilesWithExtensionStep("WebLogicCommon", "java")
      setTargetPath("org.migrorsample.common/src/main/java")
    ,
    FilesWithExtensionStep("WebLogicControls", "jcs")
      setTargetPath("org.migrorsample.services/src/main/java")
      add(
        WebLogicControlReferenceStep(),
        JcsMigrationStep(),
        FullyQualifiedTypesCleanupStep(),
        ThrowsSignatureCleanupStep(),
        WebLogicAnnotationsCleanupStep(),
        // ReplaceStep(Replacers("rules.xml")),
        StaticLoggerCorrectionStep()
      )
    ,
    FilesWithExtensionStep("WebLogicControls", "java")
      setTargetPath("org.migrorsample.services/src/main/java")
      add(
        FullyQualifiedTypesCleanupStep(),
        WebLogicControlInterfaceStep())
  )
}

object SampleMigration {

  private def setContext {
    Context.put(Context.SOURCE_PATH, "sample/WebLogicProject")
  }

  def main(args: Array[String]): Unit = {
    setContext
    //(new Migror(new SampleMigration)).execute
    Replacers(new File("SampleRules.scala"))
  }
}