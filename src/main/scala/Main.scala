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

package com.bloomlife.fbrules

import play.api.libs.json._

import com.bloomlife.fbrules.types._
import com.bloomlife.fbrules.Implicits._

object Main {
  def main(args: Array[String]) {
    val msgDef = FbObject(
      "content" -> FbRequired(FbString(minLength=Some(10), maxLength=Some(255)))
      )

    val userDef = FbObject(
      "name" -> FbRequired(FbString(minLength=Some(4), maxLength=Some(64))),
      "email" -> FbRequired(FbString()),
      "age" -> FbOptional(FbInteger(min=Some(18))),
      "msgs" -> FbOptional(FbCollection(mesgId => msgDef))
      )

    val schema = FbObject(
      "users" -> FbOptional(FbCollection(userId => userDef)),
      "messages" -> FbOptional(
        FbCollection(userId => FbCollection(msgId => msgDef)))
      )

    val rules = Rules.generate(schema)
    println(Json.prettyPrint(rules))

    val v1: IntValue = 17 + 12
    val v2: IntValue = "Hello".length
    val cond: BoolValue = v1 / 12 === v2 === false
    println(cond.toJS)
  }
}
