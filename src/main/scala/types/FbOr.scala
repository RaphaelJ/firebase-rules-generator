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

import com.bloomlife.fbrules.ruleexpr.BoolExpr

object FbOr {
  /** Creates a `FbNode` that only accepts values that validate one of the given
    * `nodes` values.
    */
  def apply(nodes: FbNode*): FbNode = {
    /** Combines the boolean expressions of the given sequence with the given
      * binary operator. */
    def reduceWith(
      exprs: Seq[Option[BoolExpr]], op: (BoolExpr, BoolExpr) => BoolExpr
      ): Option[BoolExpr] = {

      exprs.collect { case Some(v) => v }.reduceOption(op)
    }

    // Combining validatation rules is easy, we just need to merge them with
    // the `||` operator.
    def reduceWithOr(exprs: Seq[Option[BoolExpr]]) = reduceWith(exprs, (_ || _))
    val validate = reduceWithOr(nodes.map(_.validate))

    // Combining the read and writes rule are more tricky: we need to enforce
    // the rule only if it matches the associated node.
    val read = reduceWithOr(nodes.map(node => {
        (node.read, node.validate) match {
          case (Some(r), Some(v)) => Some(v && r)
          case (Some(r), None) => Some(r)
          case (None, _) => None
        }
      }))
    val write = reduceWithOr(nodes.map(node => {
        (node.write, node.validate) match {
          case (Some(w), Some(v)) => Some(v && w)
          case (Some(w), None) => Some(w)
          case (None, _) => None
        }
      }))

    // Current implementation does not support custom rules. Throws an exception
    // if any nested node generated custom rules.
    val customRules = nodes.
      toList.
      traverseS(node => {
        for {
          rules <- node.customRules
        } yield {
          if (!rules.fields.isEmpty) {
            throw new UnsupportedOperationException(
              "Custom rules are not supported when using `FbOr()`.")
          }
        }
      }).
      map(_ => JsObject.empty)

    FbNode(validate, read, write, customRules)
  }
}
