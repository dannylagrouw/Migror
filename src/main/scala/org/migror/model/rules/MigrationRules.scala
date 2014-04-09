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
package org.migror.model.rules

import org.migror.model.Context
import org.migror.steps.replace.{RegexReplacer, TextReplacer, Replacer}

class MigrationRules(val ruleSets: List[RuleSet])

object MigrationRules {
  def apply(ruleSets: RuleSet*) = new MigrationRules(ruleSets.toList)
}

trait RuleSetAttribute

class RuleSet(
  val fileFilter: Option[String] = None,
  val imports: List[String] = List.empty[String],
  val rules: List[Rule] = List.empty[Rule],
  val multiLineRules: List[MultiLineRule] = List.empty[MultiLineRule],
  val enumSetterRules: List[EnumSetterRule] = List.empty[EnumSetterRule]) {

  @deprecated
  def importsList = imports

  def allRules: List[AbstractRule] = rules ::: multiLineRules ::: enumSetterRules

  def replacers: List[Replacer] = allRules.map(_.replacer)
}

case class FileFilter(var fileFilter: String) extends RuleSetAttribute
case class Imports(var imports: String*) extends RuleSetAttribute

object RuleSet {
  def apply(attrs: RuleSetAttribute*) = {
    var fileFilter: Option[String] = None
    var imports = List.empty[String]
    var rules: List[Rule] = List.empty[Rule]
    var multiLineRules: List[MultiLineRule] = List.empty[MultiLineRule]
    var enumSetterRules: List[EnumSetterRule] = List.empty[EnumSetterRule]
    attrs.foreach(_ match {
      case f: FileFilter => fileFilter = Some(f.fileFilter)
      case i: Imports => imports = i.imports.toList
      case r: MultiLineRule => multiLineRules ::= r
      case r: EnumSetterRule => enumSetterRules ::= r
      case r: Rule => rules ::= r
    })
    new RuleSet(fileFilter, imports, rules.reverse, multiLineRules.reverse, enumSetterRules.reverse)
  }
}

trait AbstractRule {
  def replacer: Replacer
}

class Rule(val find: String, _replace: String, val regex: Boolean = false, val replaceWithComment: Boolean = false) extends AbstractRule with RuleSetAttribute {
  def replace = if (replaceWithComment) "// " + find else _replace

  def replacer =
    if (regex)
      new RegexReplacer(find, replace)
    else
      new TextReplacer(find, replace)
}

object Rule {
  def apply(find: String, replace: String, regex: Boolean = false, replaceWithComment: Boolean = false) =
    new Rule(find, replace, regex, replaceWithComment)
}

case class MultiLineRule(override val find: String, override val replace: String, override val replaceWithComment: Boolean = false)
     extends Rule(find, replace, false, replaceWithComment) {
  override def replacer = new RegexReplacer(find, replace)
  //TODO override replace?
}

class EnumSetterRule(val attribute: String, _enumType: String = "", _sourceAttribute: String = "") extends AbstractRule {
  def enumType = if (_enumType.isEmpty) attribute else _enumType
  def sourceAttribute = if (_sourceAttribute.isEmpty) attribute else _sourceAttribute
  override def replacer: Replacer = {
    Context.add(Context.ADDITIONAL_CLASSES, enumType)
    new TextReplacer(
      "returnValue.set%s(value.get%s())".format(attribute, sourceAttribute),
      "returnValue.set%s(%s.valueOf(value.get%s() != null ? value.get%s().getValue() : null))".format(
        attribute, enumType, sourceAttribute, sourceAttribute))
  }
}

object EnumSetterRule {
  def apply(attribute: String, enumType: String = "", sourceAttribute: String = "") =
    new EnumSetterRule(attribute, enumType, sourceAttribute)
}
