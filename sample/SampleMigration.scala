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

import org.migror.model.rules._
import org.migror.model.{StepList, Context, Migration}
import org.migror.steps.replace.{ReplaceStep, Replacers}
import org.migror.steps.{PackageRenameStep, FileDeleteStep, CreateTempDirStep}
import org.migror.tools.Migror
import org.migror.steps.files.FilesWithExtensionStep
import org.migror.steps.files.FilesWithExtensionStep._
import org.migror.steps.weblogic._
import java.io.File

val rules = MigrationRules(
  RuleSet(
    FileFilter(".*SampleControl.*"),
    Imports("sample.util.DateHelper", "java.sql.Date"),
    Rule("([ \\t]+)pstmt\\.setDate\\((.*), (.*)\\)(.*)",
         "$1pstmt\\.setDate\\($2, DateHelper.toSql\\($3\\)\\)$4",
         true),
    Rule("@EJB()",
         "@EJB")
  ),
  RuleSet(
    FileFilter(".*SamplePerson.*"),
    Rule("\"Hello",
         "\"Hi")
  )
)

val classesToIncludeInRename = List("Sample.*")
val classesToExcludeInRename = List(".*DontRename")

val packageRenameSteps = StepList(
  PackageRenameStep("sample.rename.me", "sample.is.renamed", classesToIncludeInRename, classesToExcludeInRename)
)

Context.put(Context.SOURCE_PATH, "WebLogicProject")
Context.put(Context.JAXWS_BINDING_FILE_TEMPLATE, "org.migrorsample.schemas/binding_%s.xjb")

//TODO rename Step to ...
//TODO refactor DSL like rules
Migration(
  // create a temp dir where migrated files will end up
  CreateTempDirStep("migror-sample-migration", Context.TARGET_PATH)
  ,
  FilesWithExtensionStep("WebLogicCommon", "java")
    setTargetPath("org.migrorsample.common/src/main/java")
    add(
      packageRenameSteps
    )
  ,
  FilesWithExtensionStep("WebLogicControls", "jcs")
    setTargetPath("org.migrorsample.services/src/main/java")
    add(
      WebLogicControlReferenceStep(),
      JcsMigrationStep(),
      FullyQualifiedTypesCleanupStep(),
      ThrowsSignatureCleanupStep(),
      WebLogicAnnotationsCleanupStep(),
      ReplaceStep(Replacers(rules)),
      StaticLoggerCorrectionStep(),
      packageRenameSteps
    )
  ,
  FilesWithExtensionStep("WebLogicControls", "java")
    setTargetPath("org.migrorsample.services/src/main/java")
    add(
      FullyQualifiedTypesCleanupStep(),
      WebLogicControlInterfaceStep(),
      ReplaceStep(Replacers(rules)),
      packageRenameSteps
    )
  ,
  FilesWithExtensionStep("WebLogicWeb/webservices", "jws")
    setTargetPath("org.migrorsample.web/src/main/java")
    add(
      WebLogicControlReferenceStep(),
      JwsMigrationStep(),
      FullyQualifiedTypesCleanupStep(),
      ThrowsSignatureCleanupStep(),
      WebLogicAnnotationsCleanupStep(),
      ReplaceStep(Replacers(rules)),
      StaticLoggerCorrectionStep(),
      packageRenameSteps
    )
  ,
  FileDeleteStep("org.migrorsample.common/src/main/java") { file =>
    file.getName.endsWith("SampleRemove.java")
  }
).execute

