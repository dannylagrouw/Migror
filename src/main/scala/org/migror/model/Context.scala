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
import scala.collection._
import org.migror.internal.{LangUtils, Logging}
import LangUtils._
import collection.JavaConverters
import java.io.{File, FileReader}

/**
 * The context within which a {@link Migration} will be executed.
 * Facilitates exchange of information between different
 * {@link Step migration steps}.
 */
object Context extends Logging {

  val PROPERTIES_FILE_VAR = "migror.properties"
  val PROPERTIES_FILE_NAME = "migror.properties"
  val SOURCE_PATH = "source.path"
  val TARGET_PATH = "target.path"
  val ADDITIONAL_CLASSES = "additional.classes.to.include"
  val JAXWS_BINDING_FILE_TEMPLATE = "jaxws.binding.file.template"
  var map = mutable.Map.empty[String, Any]

  {
    val properties = new Properties
    val propertiesFile = nullOr(System.getProperty(PROPERTIES_FILE_VAR), PROPERTIES_FILE_NAME)
    try {
      properties.load(new FileReader(propertiesFile))
      setProperties(properties)
    } catch {
      case e: Exception => warn("Geen migror.properties gevonden in huidige dir of via -Dmigror.properties.")
    }
  }

  def setProperties(properties: Properties) = {
    JavaConverters.asScalaSetConverter(properties.keySet).asScala.foreach { key =>
      map(key.asInstanceOf[String]) = properties.get(key)
    }
  }

  def contains(key: String) = map.contains(key)

  def getString(key: String): Option[String] = map.get(key).asInstanceOf[Option[String]]
  
  def getString(key: String, defaultValue: String): String = map.getOrElse(key, defaultValue).asInstanceOf[String]

  def getBoolean(key: String): Boolean = getString(key) match {
    case Some("true") => true
    case _ => false
  }

  def put(key: String, value: Any) {
    map(key) = value
  }

  /**
   * Adds <code>value</code> to the List under <code>key</code>.
   * If no List exists yet, a new one is added first.
   */
  def add[T <: Any](key: String, value: T) {
    map(key) = getList(key) ++ List(value)
  }

  /**
   * Adds <code>value</code> to the List under <code>key</code>,
   * if it is not already in the List.
   */
  def addUnique[T <: Any](key: String, value: T) {
    val list = getList(key)
    if (!list.contains(value)) {
      add(key, value)
    }
  }

  /**
   * Returns the List under <code>key</code>.
   * If no List exists yet, a new empty List is returned.
   */
  def getList[T <: Any](key: String): List[T] = map.get(key) match {
    case Some(l: List[T]) => l
    case None => List.empty[T]
    case _ => throw new ContextTypeException("No List of required type present under %s".format(key))
  }

  def getFile(key: String): File = {
    map.get(key) match {
      case Some(f: File) => f
      case Some(s: String) => new File(s)
      case value => throw new ContextTypeException("No valid file name found in property %s: %s".format(key, value))
    }
  }

  def removeValue[T <: Any](value: T) {
    map.foreach { entry => entry._2 match {
      case v: T =>
        if (v == value) {
          println("Found value under key " + entry._1)
        }
      case s: String =>
        if (s == value.toString) {
          println("Found string value under key " + entry._1)
        }
    }
    }
  }

  /**
   * Returns the entire contents of the Context as a formatted String,
   * for debugging purposes.
   */
  def dump: String = {
    map.map { entry =>
      entry._1 + " = " + (entry._2 match {
        case l: List[_] => l.mkString("[\n - ", "\n - ", "]")
        case value => value.toString
      })
    }.toList.sorted.mkString("\n")
  }

  /**
   * Replaces variables in the specified template with their values in
   * Context. Variables must be in ${...} notation, e.g.
   * <p>
   * <code>The value of some-key is ${some-key}.</code>
   */
  def replaceVars(template: String) = {
    map.foldLeft(template) { (s, entry) =>
      s.replaceAll("\\$\\{" + entry._1 + "\\}", entry._2.toString)
    }
  }

  def clear {
    map.clear
  }

  def sourcePath = getFile(SOURCE_PATH)

  def targetPath = getFile(TARGET_PATH)
}