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

import japa.parser.JavaParser
import java.io.{ByteArrayInputStream, File}
import japa.parser.ast.PackageDeclaration
import org.migror.javaparser.CompilationUnitWrapper._
import org.apache.commons.io.FilenameUtils

/**
 * A file containing Java source code, that must be migrated. Besides location information, objects of
 * this class also hold the parsed file contents, to be manipulated during
 * migration processing steps.
 * @param sourcePath start path from which files are being migrated. If this
 * migration file is located in a child folder beneath this start path, it will be
 * migrated to the same relative folder under targetPath.
 * @param sourceFile the file to be migrated.
 * @param targetPath the start path to which files are being migrated.
 */
class JavaFile(sourcePath: String, sourceFile: File, targetPath: File) extends MigrationFile(sourcePath, sourceFile, targetPath) {

  var compilationUnit = JavaParser.parse(sourceFile)

  /**
   * Returns the Java source code as text string from the parsed file.
   */
  override def contents = compilationUnit.toString

  /**
   * Replaces this file's contents with new Java source code. This code
   * will be parsed and checked immediately.
   */
  override def contents_=(javaSource: String) {
    compilationUnit = JavaParser.parse(new ByteArrayInputStream(javaSource.getBytes))
  }

  /**
   * Returns the full path and file name where this file will be saved
   * after migration. This name will be deduced from the Java source's
   * package and class name.
   */
  override def targetFile = {
    val packageDir = packageToDir(compilationUnit.getPackage)
    val className = compilationUnit.firstClassName + ".java"
    new File(targetPath, packageDir + className)
  }

  private def packageToDir(packageDeclaration: PackageDeclaration) =
      packageDeclaration.getName.toString.replaceAll("\\.", "/") + "/"
  
}

object JavaFile {
  private val EXTENSIONS = List(
      "java",
      "jws",
      "jcx",
      "jcs",
      "jpf",
      "ejb",
      "app")

  def isJavaFile(sourceFile: File): Boolean =
    EXTENSIONS.contains(FilenameUtils.getExtension(sourceFile.getName()))

}