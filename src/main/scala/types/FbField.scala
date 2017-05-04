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

/**
 *  @param validate a Javascript expression that validates the field's content.
 */
abstract class FbField(validate: Option[BoolExpr]) extends FbNode {
  def validateIf(newCond: BoolExpr): FbField = new FbField(
    validate match {
      case Some(cond) => Some(cond && newCond)
      case None       => Some(newCond)
    }
  ) { }

  override def rules: Generator[JsObject] = {
    validate match {
      case Some(cond) => JsObject(Seq(".validate" -> JsString(cond.toJS)))
      case None       => JsObject(Seq())
    }
  }.pure[Generator]
}
