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

import scala.language.implicitConversions

/** Provides Scala expressions that maps to Javascript expressions that can be
 *  used to define validation rules as defined in
 *  <https://firebase.google.com/docs/reference/security/database/>.
 */
sealed trait RuleExpr

sealed trait Value extends RuleExpr {
  def toJS: String
}

// Base traits for values

/** Expressions that can be compared with themselves. */
sealed trait Equalable extends Value {
  /** The type of the expression. */
  type Self <: Value

  def ===(other: Self) = {
    val js = this.toJS
    new BoolValue() { def toJS = s"(${js}===${other.toJS})" }
  }
}

/** Any expression that returns a boolean value. */
sealed trait BoolValue extends Equalable {
  type Self = BoolValue

  def &&(other: BoolValue) = {
    val js = this.toJS
    new BoolValue() { def toJS = s"(${js}&&${other.toJS})" }
  }

  def ||(other: BoolValue) = {
    val js = this.toJS
    new BoolValue() { def toJS = s"(${js}||${other.toJS})" }
  }

  def not = {
    val js = this.toJS
    new BoolValue() { def toJS = s"(!${js})" }
  }

  // def ifelse[T <: RuleExpr](ifTrue: T, ifFalse: T) = {
  //   val js = this.toJS
  //   new T() { def toJS = s"(${js}?${ifTrue.toJS}:${ifFalse.toJS})" }
  // }
}

/** Any expression that returns an integer value. */
sealed trait IntValue extends Value with Equalable {
  type Self = IntValue

  def +(other: IntValue) = {
    val js = this.toJS
    new IntValue() { def toJS = s"(${js}+${other.toJS})" }
  }

  def -(other: IntValue) = {
    val js = this.toJS
    new IntValue() { def toJS = s"(${js}-${other.toJS})" }
  }

  def *(other: IntValue) = {
    val js = this.toJS
    new IntValue() { def toJS = s"(${js}*${other.toJS})" }
  }

  def /(other: IntValue) = {
    val js = this.toJS
    new IntValue() { def toJS = s"(${js}/${other.toJS})" }
  }

  def %(other: IntValue) = {
    val js = this.toJS
    new IntValue() { def toJS = s"(${js}%${other.toJS})" }
  }
}

/** Any expression that returns a string value. */
sealed trait StringValue extends Value with Equalable {
  type Self = StringValue

  def length = {
    val js = this.toJS
    new IntValue() { def toJS = s"${js}.length" }
  }

  def contains(str: StringValue) = {
    val js = this.toJS
    new BoolValue() { def toJS = s"${js}.contains(${str.toJS})" }
  }

  def beginWith(str: StringValue) = {
    val js = this.toJS
    new BoolValue() { def toJS = s"${js}.beginWith(${str.toJS})" }
  }

  def endsWith(str: StringValue) = {
    val js = this.toJS
    new BoolValue() { def toJS = s"${js}.endsWith(${str.toJS})" }
  }

  def matches(regex: String) = {
    val js = this.toJS
    new BoolValue() { def toJS = s"${js}.match(${regex})" }
  }

  def +(other: StringValue) = {
    val js = this.toJS
    new StringValue() { def toJS = s"(${js}+${other.toJS})" }
  }
}

// Literals

case class BoolLiteral(value: Boolean) extends BoolValue {
  def toJS = s"${value}"
}

case class IntLiteral(value: Int) extends IntValue {
  def toJS = s"${value}"
}

case class StringLiteral(value: String) extends StringValue {
  def toJS = s"'${value}'"
}

// Implicit conversions to literals

object Implicits {
  implicit def fromBoolean(value: Boolean) = BoolLiteral(value)

  implicit def fromInt(value: Int) = IntLiteral(value)

  implicit def fromString(value: String) = StringLiteral(value)
}

// `auth` object

object Auth {
  def provider = new StringValue() { def toJS = "auth.provider" }

  def uid = new StringValue() { def toJS = "auth.uid" }

  object Token {
    def email = new StringValue() { def toJS = "auth.token.email" }

    def emailVerified = new BoolValue() {
      def toJS = "auth.token.email_verified"
    }

    def name = new StringValue() { def toJS = "auth.token.name" }

    def sub = new StringValue() { def toJS = "auth.token.sub" }
  }

  def token = Token
}
