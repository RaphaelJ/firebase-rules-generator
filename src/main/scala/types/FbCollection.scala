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
import scalaz.State.{get, put}

import com.bloomlife.fbrules.Rules.Generator
import com.bloomlife.fbrules.ruleexpr.LocationVariable

object FbCollection {
  def apply(coll: LocationVariable => FbNode): FbNode = {
    // Adds a `'$location': {..}` node to the default node's rules.
    val collRules: Generator[JsObject] =
      for {
        // Generates a new `$location` variable.
        currId <- get
        _ <- put(currId + 1)
        currIdVar = LocationVariable(s"id_${currId}")

        nestedRules <- coll(currIdVar).rules
      } yield JsObject(Seq(currIdVar.toJS -> nestedRules))

    FbNode(customRules=collRules)
  }
}
