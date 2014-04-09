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
package org.migror.model.weblogic

import java.io.File
import org.migror.model.Context


case class WsdlWebService(wsdlName: String, webServiceName: String, sourcePath: String, targetPath: String, nameSpaceFromWsdl: String) {
  val customBindingFile = new File(Context.targetPath, Context.getString(Context.JAXWS_BINDING_FILE_TEMPLATE, "%s").format(wsdlName))
  def customBindingFileExists = customBindingFile.exists
}
