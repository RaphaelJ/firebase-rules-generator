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
import scalaz.std.list._
import scalaz.syntax.traverse._

import com.bloomlife.fbrules.Rules.Generator
import com.bloomlife.fbrules.ruleexpr.{BoolExpr, NewData}
import com.bloomlife.fbrules.ruleexpr.Implicits._

object FbObject {
  def apply(children: (String, FbObjectField)*): FbNode = {
    // Requires the required fields to be present
    val requiredChildNames = children.
      filter(_._2.isRequired).
      map(child => fromString(child._1)).
      toSeq

    val childrenValidate: Option[BoolExpr] =
      if (requiredChildNames.isEmpty) {
        None
      } else {
        Some(NewData.hasChildren(requiredChildNames))
      }

    // Generates the nested children rules
    val childRules: Generator[JsObject] =
      for {
        // Recursively generates the rules for the children.
        childrenRules <- children.
          toList.
          traverseS {
            case (key, child) =>
              for (rules <- child.node.rules) yield (key -> rules)
          }.
          map(JsObject(_))

        // Generates a node that never validates, to only allow the previously
        // allowed field names.
        invalidNodeRules <- FbNode(validate=Some(false)).rules
      } yield childrenRules + ("$other" -> invalidNodeRules)

    FbNode(validate=childrenValidate, customRules=childRules)
  }
}

sealed trait FbObjectField {
  val node: FbNode
  val isRequired: Boolean
}

case class FbRequired(node: FbNode) extends FbObjectField {
  val isRequired = true
}

case class FbOptional(node: FbNode) extends FbObjectField {
  val isRequired = false
}

// Implicits

object Implicits {
  /** Utility class to help creating `FbRequired` and `FbOptional` fields. */
  implicit class FbObjectFieldTitle(value: String) {
    def :=(node: FbNode): (String, FbRequired) = value -> FbRequired(node)

    def ?=(node: FbNode): (String, FbOptional) = value -> FbOptional(node)
  }
}
