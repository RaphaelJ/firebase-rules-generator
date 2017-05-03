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
import com.bloomlife.fbrules.LocationVariable

case class FbCollection(coll: LocationVariable => FbNode) extends FbNode {
  def rules: Generator[JsObject] =
    for {
      // Generates a new `$location` variable.
      currId <- get
      _ <- put(currId + 1)
      currIdStr = s"$$id_${currId}"

      nestedRules <- coll(LocationVariable(currIdStr)).rules
    } yield {
      JsObject(Seq(currIdStr -> nestedRules))
    }
}
