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

import org.migror.model.Migration

/**
 * Standalone tool to start a Migration.
 */
class Migror(migration: Migration) {

  /**
   * Executes all steps in this Migration.
   */
  def execute: Unit = migration.execute

  def this(migrationClassName: String) = this(java.lang.Class.forName(migrationClassName).newInstance.asInstanceOf[Migration])

}

object Migror {

  def startFromCommandLine(args: Array[String]): Int = {
    var returnCode = 1
    try {
      if (args.length == 1) {
        new Migror(args(0)).execute
        returnCode = 0
      } else {
        System.out.println("Geef classnaam van migratielezer als parameter")
      }
    } catch {
      case e: Exception => e.printStackTrace()
    }
    return returnCode
  }

  def main(args: Array[String]): Unit = {
    var returnCode = startFromCommandLine(args)
    System.exit(returnCode);
  }
}