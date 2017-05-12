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

package com.bloomlife.fbrules.ruleexpr

import scala.language.implicitConversions

/** Provides Scala expressions that maps to Javascript expressions that can be
 *  used to define validation rules as defined in
 *  <https://firebase.google.com/docs/reference/security/database/>.
 */
sealed trait RuleExpr {
  def toJS: String
}

// Base traits for values

/** Expressions that can be compared with themselves. */
sealed trait EqualableExpr extends RuleExpr {
  /** The type of the expression. */
  type Self <: RuleExpr

  def ===(other: Self) = {
    val js = this.toJS
    new BoolExpr() { def toJS = s"(${js}===${other.toJS})" }
  }
}

/** Any expression that returns a boolean value. */
sealed trait BoolExpr extends EqualableExpr {
  type Self = BoolExpr

  def &&(other: BoolExpr) = {
    val js = this.toJS
    new BoolExpr() { def toJS = s"(${js}&&${other.toJS})" }
  }

  def ||(other: BoolExpr) = {
    val js = this.toJS
    new BoolExpr() { def toJS = s"(${js}||${other.toJS})" }
  }

  def not = {
    val js = this.toJS
    new BoolExpr() { def toJS = s"(!${js})" }
  }

  def unary_! = not

  // TODO: implement the ternary operator.
  // def ifelse[T <: RuleExpr](ifTrue: T, ifFalse: T) = {
  //   val js = this.toJS
  //   new T() { def toJS = s"(${js}?${ifTrue.toJS}:${ifFalse.toJS})" }
  // }
}

/** Any expression that returns an numeric value. */
sealed trait NumberExpr extends EqualableExpr {
  type Self = NumberExpr

  def +(other: NumberExpr) = {
    val js = this.toJS
    new NumberExpr() { def toJS = s"(${js}+${other.toJS})" }
  }

  def -(other: NumberExpr) = {
    val js = this.toJS
    new NumberExpr() { def toJS = s"(${js}-${other.toJS})" }
  }

  def *(other: NumberExpr) = {
    val js = this.toJS
    new NumberExpr() { def toJS = s"(${js}*${other.toJS})" }
  }

  def /(other: NumberExpr) = {
    val js = this.toJS
    new NumberExpr() { def toJS = s"(${js}/${other.toJS})" }
  }

  def %(other: NumberExpr) = {
    val js = this.toJS
    new NumberExpr() { def toJS = s"(${js}%${other.toJS})" }
  }

  def >(other: NumberExpr) = {
    val js = this.toJS
    new BoolExpr() { def toJS = s"(${js}>${other.toJS})" }
  }

  def <(other: NumberExpr) = {
    val js = this.toJS
    new BoolExpr() { def toJS = s"(${js}<${other.toJS})" }
  }

  def >=(other: NumberExpr) = {
    val js = this.toJS
    new BoolExpr() { def toJS = s"(${js}>=${other.toJS})" }
  }

  def <=(other: NumberExpr) = {
    val js = this.toJS
    new BoolExpr() { def toJS = s"(${js}<=${other.toJS})" }
  }
}

/** Any expression that returns a string value. */
sealed trait StringExpr extends EqualableExpr {
  type Self = StringExpr

  def length = {
    val js = this.toJS
    new NumberExpr() { def toJS = s"${js}.length" }
  }

  def contains(str: StringExpr) = {
    val js = this.toJS
    new BoolExpr() { def toJS = s"${js}.contains(${str.toJS})" }
  }

  def beginWith(str: StringExpr) = {
    val js = this.toJS
    new BoolExpr() { def toJS = s"${js}.beginWith(${str.toJS})" }
  }

  def endsWith(str: StringExpr) = {
    val js = this.toJS
    new BoolExpr() { def toJS = s"${js}.endsWith(${str.toJS})" }
  }

  def matches(regex: String) = {
    val js = this.toJS
    new BoolExpr() { def toJS = s"${js}.matches(${regex})" }
  }

  def +(other: StringExpr) = {
    val js = this.toJS
    new StringExpr() { def toJS = s"(${js}+${other.toJS})" }
  }
}

// Literals

case class BoolLiteral(value: Boolean) extends BoolExpr {
  def toJS = s"${value}"
}

case class IntLiteral(value: Int) extends NumberExpr {
  def toJS = s"${value}"
}

case class DoubleLiteral(value: Double) extends NumberExpr {
  def toJS = s"${value}"
}

case class StringLiteral(value: String) extends StringExpr {
  def toJS = s"'${value}'"
}

// Implicit conversions to literals

object Implicits {
  implicit def fromBoolean(value: Boolean) = BoolLiteral(value)

  implicit def fromInt(value: Int) = IntLiteral(value)

  implicit def fromDouble(value: Double) = DoubleLiteral(value)

  implicit def fromString(value: String) = StringLiteral(value)
}

// `auth` object

object Auth {
  def isLoggedIn = new BoolExpr() { def toJS = "auth != null" }

  def provider = new StringExpr() { def toJS = "auth.provider" }

  def uid = new StringExpr() { def toJS = "auth.uid" }

  object Token {
    def email = new StringExpr() { def toJS = "auth.token.email" }

    def emailVerified = new BoolExpr() {
      def toJS = "auth.token.email_verified"
    }

    def name = new StringExpr() { def toJS = "auth.token.name" }

    def sub = new StringExpr() { def toJS = "auth.token.sub" }
  }

  def token = Token
}

// $location's variables

case class LocationVariable(name: String) extends StringExpr {
  def toJS = s"$$${name}"
}

// Data snapshots

/** Contains a snapshot of the data at the given path.
 *
 *  @param origin the reference node that is used as the root of the path.
 *  @param moves the moves to be applied to the reference node, in reverse
 *               order.
 */
case class DataSnapshot(origin: OriginNode, moves: Seq[PathMove] = Seq.empty) {

  /** Generates the Javascript expression to the referenced node */
  private def _jsPath: String = {
    val originPath = origin.toJSPath

    if (moves.isEmpty) {
      originPath
    } else {
      val movePath = moves.reverse.
        map(_.toJSPath).
        mkString(".")

      s"${originPath}.${movePath}"
    }
  }

  def child(node: StringExpr) = copy(moves=Child(node) +: moves)

  def /(node: StringExpr) = child(node)

  def parent = {
    // If the last move is a child, just removes it.
    moves match {
      case Child(_) :: tail => copy(moves=tail)
      case _                => copy(moves=Parent() +: moves)
    }
  }

  def hasChild(childPath: StringExpr) = new BoolExpr() {
    def toJS = s"${_jsPath}.hasChild('${childPath}')"
  }

  /** Returns a true value if the node has any children. */
  def hasChildren = new BoolExpr() { def toJS = s"${_jsPath}.hasChildren()" }

  /** Returns a true value if the node has all the listed children. */
  def hasChildren(children: Seq[StringExpr]) = new BoolExpr() {
    def toJS = {
      val childrenStr = children.
        map(child => child.toJS).
        mkString(",")

      s"${_jsPath}.hasChildren(${childrenStr})"
    }
  }

  def exists = new BoolExpr() { def toJS = s"${_jsPath}.exists()" }

  def isBoolean = new BoolExpr() { def toJS = s"${_jsPath}.isBoolean()" }

  def asBoolean = new BoolExpr() { def toJS = s"${_jsPath}.val()" }

  def isNumber = new BoolExpr() { def toJS = s"${_jsPath}.isNumber()" }

  def asNumber = new NumberExpr() { def toJS = s"${_jsPath}.val()" }

  def isString = new BoolExpr() { def toJS = s"${_jsPath}.isString()" }

  def asString = new StringExpr() { def toJS = s"${_jsPath}.val()" }
}

sealed trait OriginNode { def toJSPath: String }

object DataNode extends OriginNode { def toJSPath = "data" }

object NewDataNode extends OriginNode { def toJSPath = "newData" }

object RootNode extends OriginNode { def toJSPath = "root" }

object Data extends DataSnapshot(DataNode)

object NewData extends DataSnapshot(NewDataNode)

object Root extends DataSnapshot(RootNode)

sealed trait PathMove { def toJSPath: String }

case class Child(path: StringExpr) extends PathMove {
  def toJSPath = s"child(${path.toJS})"
}

case class Parent() extends PathMove { def toJSPath = s"parent()" }
