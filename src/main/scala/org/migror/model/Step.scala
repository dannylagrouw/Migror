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
import org.migror.internal.LangUtils.withDoReturn

/**
 * A Migration step. All steps in a migration are executed consecutively (via {@link #execute}).
 * Each step may have child steps. Child steps will be executed recursively just
 * before this step itself is being executed.
 */
abstract class Step extends Logging {

  val name = getClass.getName

  var steps = List.empty[Step]

  var parent: Option[Step] = None

  /**
   * Executes just this step, without its child steps.
   */
  def executeThisStepOnly: Unit

  def execute {
    if (skip) {
      info("Step %s will be skipped, property %s.skip is true".format(getClass.getSimpleName, getClass.getSimpleName))
    } else {
      info("Begin execute")
      begin
      steps.foreach(_.execute)
      executeThisStepOnly
    }
  }

  def skip = Context.getBoolean(getClass.getSimpleName + ".skip")

  /**
   * Adds one or more child steps to this step.
   */
  def add(childSteps: Step*): Step = {
    childSteps.foreach { stepOrSteps => stepOrSteps match {
      case stepList: StepList =>
        add(stepList.stepList:_*)
      case step: Step =>
        steps ++= List(step)
        step.parent = Some(this)
      }
    }
    this
  }

  def begin {}
}