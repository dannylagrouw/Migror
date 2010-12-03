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

import java.util.Properties
import java.io.FileReader
import scala.collection._
import org.migror.internal.{LangUtils, Logging}
import LangUtils._
import collection.JavaConverters

class Context extends Logging {

  val PROPERTIES_FILE_VAR = "migror.properties"
  val PROPERTIES_FILE_NAME = "migror.properties"
  var map = mutable.Map.empty[String, Any]

  {
    val properties = new Properties
    val propertiesFile = nullOr(System.getProperty(PROPERTIES_FILE_VAR), PROPERTIES_FILE_NAME)
    try {
      properties.load(new FileReader(propertiesFile))
      loadProperties(properties)
    } catch {
      case e: Exception => warn("Geen migror.properties gevonden in huidige dir of via -Dmigror.properties.")
    }
  }

  def loadProperties(properties: Properties) = {
    JavaConverters.asScalaSetConverter(properties.keySet).asScala.foreach { key =>
      map(key.asInstanceOf[String]) = properties.get(key)
    }
  }
}