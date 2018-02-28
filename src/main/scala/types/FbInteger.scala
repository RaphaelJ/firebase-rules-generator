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
import com.bloomlife.fbrules.ruleexpr.{NewData}
import com.bloomlife.fbrules.ruleexpr.Implicits._

object FbInteger {
  def apply(min: Option[Int] = None, max: Option[Int] = None): FbNode = {
    def toDouble(optInt: Option[Int]) = optInt.map(_.toDouble)
    FbNumber(toDouble(min), toDouble(max)).
      validateIf((NewData.asNumber % 1) === 0.0)
  }
}
