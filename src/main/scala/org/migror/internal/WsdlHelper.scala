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
package org.migror.internal

import org.migror.steps.files.MigrationFile
import org.apache.commons.lang.StringUtils


object WsdlHelper {
  /**
   * Haalt de target namespace uit het opgegeven WSDL-bestand, waarbij alle
   * /-tekens door punten worden vervangen.
   * <p>
   * Bijvoorbeeld: een WSDL met als namespace <br>
   * <code>http://www.openuri.org/more/evenmore</code> <br>
   * leidt tot resultaat: <br>
   * <code>openuri.org.more.evenmore</code>
   *
   * @param wsdlBestand
   * @return
   */
  def extractNameSpace(wsdlBestand: MigrationFile): String = {
      val regex = ".*targetName[-]*space=\"([^\"]+)\".*"

      val namespace = wsdlBestand.contents
        .split("\r\n")
        .find(_.matches(regex))
        .map(_.replaceAll(regex, "$1"))
        .getOrElse("")
      namespace = namespace.replace("http://www.", "");
      namespace = StringUtils.stripEnd(namespace, "/");

      String returnValue = namespace.toLowerCase().replace("/", ".");
      log.info(String.format("Namespace van WSDL %s = %s", wsdlBestand.getBronFile().getName(), returnValue));

      return returnValue;
  }

}