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

import com.bloomlife.fbrules.Rules.Generator

case class FbInteger(min: Option[Long] = None, max: Option[Long] = None)
  extends FbField {

  def validate: Option[String] = {
    var constraints = Seq("newData.isInteger")

    if (min.isDefined) {
      constraints :+= s"newData.val() >= ${min.get}"
    }

    if (max.isDefined) {
      constraints :+= s"newData.val() <= ${max.get}"
    }

    Some(constraints.mkString(" && "))
  }
}
