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
package org.migror.model

import org.migror.internal.Logging

/**
 * A migration definition, consisting of a number of migration steps that can be executed.
 */
abstract class Migration extends Logging {
  def steps: List[Step]

  def execute: Unit = {
    info("Start migration")
    steps.foreach(_.execute)
    info("End of migration")
  }

}

object Migration {
  def apply(migrationSteps: Step*) = new Migration {
    def steps = migrationSteps.toList
  }
}