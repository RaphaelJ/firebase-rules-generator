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

// Data snapshots

/** Contains a snapshot of the data at the given path.
 *
 *  @param origin the reference node that is used as the root of the path.
 *  @param moves the moves to be applied to the reference node, in reverse
 *               order.
 */
case class DataSnapshot(origin: OriginNode, moves: Seq[PathMove] = Seq.empty)
  extends RuleExpr {

  /** Generates the Javascript expression to the referenced node */
  private def _jsPath: String = {
    val originPath = origin.toJSPath
    val movePath = moves.reverse.
      map(_.toJSPath).
      mkString(".")

    s"${originPath}.${movePath}"
  }

  def child(node: String) = copy(moves=Child(node) +: moves)

  def /(node: String) = child(node)

  def parent = {
    // If the last move is a child, just removes it.
    moves match {
      case Child(_) :: tail => copy(moves=tail)
      case _                => copy(moves=Parent() +: moves)
    }
  }

  def hasChild(childPath: StringValue) = new BoolValue() {
    def toJS = s"${_jsPath}.hasChild('${childPath}')"
  }

  /** Returns a true value if the node has any children. */
  def hasChildren = new BoolValue() { def toJS = s"${_jsPath}.hasChildren()" }

  /** Returns a true value if the node has all the listed children. */
  def hasChildren(children: Seq[StringValue]) = new BoolValue() {
    def toJS = {
      val childrenStr = children.
        map(child => s"'${child.toJS}'").
        mkString(",")

      s"${_jsPath}.hasChildren(${childrenStr})"
    }
  }

  def exists = new BoolValue() { def toJS = s"${_jsPath}.exists()" }

  def isBoolean = new BoolValue() { def toJS = s"${_jsPath}.isBoolean()" }

  def asBoolean = new BoolValue() { def toJS = s"${_jsPath}.val()" }

  def isNumber = new BoolValue() { def toJS = s"${_jsPath}.isNumber()" }

  def asNumber = new IntValue() { def toJS = s"${_jsPath}.val()" }

  def isString = new BoolValue() { def toJS = s"${_jsPath}.isString()" }

  def asString = new StringValue() { def toJS = s"${_jsPath}.val()" }
}

sealed trait OriginNode { def toJSPath: String }

object DataNode extends OriginNode { def toJSPath = "data" }

object NewDataNode extends OriginNode { def toJSPath = "newData" }

object RootNode extends OriginNode { def toJSPath = "root" }

object Data extends DataSnapshot(DataNode)

object NewData extends DataSnapshot(NewDataNode)

object Root extends DataSnapshot(RootNode)

sealed trait PathMove { def toJSPath: String }

case class Child(name: String) extends PathMove {
  def toJSPath = s"child('${name}')"
}

case class Parent() extends PathMove { def toJSPath = s"parent()" }
