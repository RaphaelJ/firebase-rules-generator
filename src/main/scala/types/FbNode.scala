// Firebase Rules Generator
// Bloom Technologies Inc. Copyright 2017
//
// Authors: Raphael Javaux <raphael@bloomlife.com>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.bloomlife.fbrules.types

import play.api.libs.json._
import scalaz.syntax.applicative._

import com.bloomlife.fbrules.Rules.Generator
import com.bloomlife.fbrules.ruleexpr.BoolExpr

/** Base type for nodes of the Firebase schema.
 *
 *  @param validate a Javascript expression to validate the node's content.
 *  @param read a Javascript expression to allow to read the node's content.
 *  @param write a Javascript expression to allow to write the node's content.
 *  @param customRules an additional rule generator that will provide additional
 *                     rules than those generated for `validate`, `read` and
 *                     `write`.
 *
 *  {{{
 *    val stringNode = FbNode().validateIf(NewData.isString))
 *    val dateNode = stringNode.validateIf(
 *      NewData.asString.matches("/^[0-9]{2}[/][0-9]{2}[/][0-9]{4}$/"))
 *  }}}
 *
 */
case class FbNode(
    validate: Option[BoolExpr] = None, read: Option[BoolExpr] = None,
    write: Option[BoolExpr] = None,
    customRules: Generator[JsObject] = JsObject(Seq()).pure[Generator]
  ) {

  /** Generates a new `FbNode` from the current node type that will validate
   *  only if the new node satisfy the current node's condition AND the new
   *  condition. */
  def validateIf(newCond: BoolExpr): FbNode = copy(validate=
    validate match {
      case Some(cond) => Some(cond && newCond)
      case None       => Some(newCond)
    }
  )

  /** Generates a new node that will be readable only if the new node satisfy
   *  the current node's condition OR the new condition. */
  def readIf(newCond: BoolExpr): FbNode = copy(read=
    read match {
      case Some(cond) => Some(cond || newCond)
      case None       => Some(newCond)
    }
  )

  /** Generates a new node will be writable only if the new node satisfy the
   *  current node's condition OR the new condition. */
  def writeIf(newCond: BoolExpr): FbNode = copy(write=
    write match {
      case Some(cond) => Some(cond || newCond)
      case None       => Some(newCond)
    }
  )

  /** Generates a new node that will be readable and writable only if the new
   *  node satisfy the current node's condition OR the new condition.
   *
   *  Same as calling `readIf()` and `writeIf()`.
   */
  def accessIf(newCond: BoolExpr): FbNode = readIf(newCond).writeIf(newCond)

  /** Generates the Firebase rules for the node. */
  def rules: Generator[JsObject] = {
    for {
      customRulesObj <- customRules
    } yield {
      val validateRule =
        validate.map(cond => ".validate" -> JsString(cond.toJS))
      val readRule = read.map(cond => ".read" -> JsString(cond.toJS))
      val writeRule = write.map(cond => ".write" -> JsString(cond.toJS))

      val definedRules = Seq(validateRule, readRule, writeRule).
        collect({ case Some(rule) => rule })

      JsObject(definedRules) ++ customRulesObj
    }
  }
}
