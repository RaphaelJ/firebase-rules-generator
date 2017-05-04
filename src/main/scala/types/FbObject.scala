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

case class FbObject(childs: (String, FbObjectField)*) extends FbNode {
  override def rules: Generator[JsObject] = {
    for {
      // Recursively generates the rules for the children.
      childRules <- childs.
        toList.
        traverseS {
          case (key, child) =>
            for (rules <- child.node.rules) yield (key -> rules)
        }
    } yield {
      val requiredFields = childs.
        filter(_._2.required).
        map { case (key, _)  => s"'${key}'" }.
        mkString(",")

      JsObject(
        // Does not allow any other field.
        (".validate" -> JsString(s"newData.hasChildren([${requiredFields}])")) ::
        ("$other" -> Json.toJson(Map(".validate" -> JsFalse))) ::
        childRules
      )
    }
  }
}


sealed trait FbObjectField {
  val node: FbNode
  val required: Boolean
}

case class FbRequired(node: FbNode) extends FbObjectField {
  val required = true
}

case class FbOptional(node: FbNode) extends FbObjectField {
  val required = false
}

// Implicits

object Implicits {
  /** Utility class to help creating `FbRequired` and `FbOptional` fields. */
  implicit class FbObjectFieldTitle(value: String) {
    def :=(node: FbNode): (String, FbRequired) = value -> FbRequired(node)

    def ?=(node: FbNode): (String, FbOptional) = value -> FbOptional(node)
  }
}
