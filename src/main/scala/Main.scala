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

object Main {
  def main(args: Array[String]) {
    val msgDef = FbObject(
      "content" -> FbString(minLength=Some(10), maxLength=Some(255))
      )

    val userDef = FbObject(
      "name" -> FbString(minLength=Some(4), maxLength=Some(64)),
      "email" -> FbString(),
      "age" -> FbInteger(min=Some(18)),
      "msgs" -> FbCollection(mesgId => msgDef)
      )

    val schema = FbObject(
      "users" -> FbCollection(userId => userDef),
      "messages" -> FbCollection(userId => FbCollection(msgId => msgDef))
      )

    println(Json.prettyPrint(schema.rules))
  }
}
