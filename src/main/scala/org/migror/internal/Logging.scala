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
package org.migror.internal

import org.apache.log4j._
import org.apache.log4j.Level._

trait Logging {

  val logger = Logger.getLogger(getClass().getName());

  def debug(message: => String) = if (logger.isEnabledFor(DEBUG)) logger.debug(message)
  def debug(message: => String, ex:Throwable) = if (logger.isEnabledFor(DEBUG)) logger.debug(message,ex)

  def info(message: => String) = if (logger.isEnabledFor(INFO)) logger.info(message)
  def info(message: => String, ex:Throwable) = if (logger.isEnabledFor(INFO)) logger.info(message,ex)

  def warn(message: => String) = if (logger.isEnabledFor(WARN)) logger.warn(message)
  def warn(message: => String, ex:Throwable) = if (logger.isEnabledFor(WARN)) logger.warn(message,ex)

  def error(ex:Throwable) = if (logger.isEnabledFor(ERROR)) logger.error(ex.toString,ex)
  def error(message: => String) = if (logger.isEnabledFor(ERROR)) logger.error(message)
  def error(message: => String, ex:Throwable) = if (logger.isEnabledFor(ERROR)) logger.error(message,ex)

  def fatal(ex:Throwable) = if (logger.isEnabledFor(FATAL)) logger.fatal(ex.toString,ex)
  def fatal(message: => String) = if (logger.isEnabledFor(FATAL)) logger.fatal(message)
  def fatal(message: => String, ex:Throwable) = if (logger.isEnabledFor(FATAL)) logger.fatal(message,ex)
}
