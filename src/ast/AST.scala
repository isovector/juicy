package juicy.source.ast

import juicy.utils.Implicits._
import juicy.utils.visitor._

object Modifiers {
  type Value = Int

  // Update parse() if you change this list
  val NONE      = 0
  val PUBLIC    = 1 << 0
  val PROTECTED = 1 << 1
  val STATIC    = 1 << 2
  val EXTERN    = 1 << 3
  val NATIVE    = 1 << 4
  val ABSTRACT  = 1 << 5
  val FINAL     = 1 << 6

  def parse(str: String): Value = {
    str.toLowerCase match {
      case "public"    => PUBLIC
      case "protected" => PROTECTED
      case "static"    => STATIC
      case "extern"    => EXTERN
      case "native"    => NATIVE
      case "abstract"  => ABSTRACT
      case "final"     => FINAL
    }
  }
}

trait Expression extends Visitable
trait Statement extends Visitable
trait Definition extends Visitable
trait ImportStmnt extends Statement

trait BinaryOperator extends Expression {
  val lhs: Expression
  val rhs: Expression

  def children = Seq(lhs, rhs)
}

trait UnaryOperator extends Expression {
  val ghs: Expression

  def children = Seq(ghs)
}

case class Typename (qname: QName, isArray: Boolean=false) {
  var resolved: Option[ClassDefn] = None
  val name = qname.mkString(".")
  val brackets = if (isArray) " []" else ""
  override def toString() = s"$name$brackets"
}

case class FileNode(
  pkg: QName,
  imports: Seq[ImportStmnt],
  classes: Seq[ClassDefn]
) extends Visitable {
  def children = imports ++ classes
}

case class ClassDefn(
  name: String,
  mods: Modifiers.Value,
  extnds: Option[Typename],
  impls: Seq[Typename],
  fields: Seq[VarStmnt],
  constructors: Seq[MethodDefn],
  methods: Seq[MethodDefn],
  isInterface: Boolean = false
) extends Definition {
  def children = fields ++ constructors ++ methods
}

case class ImportClass(
  tname: Typename
) extends ImportStmnt {
  def children = Seq()
}

case class ImportPkg(
  pkg: QName
) extends ImportStmnt {
  def children = Seq()
}

case class MethodDefn(
  name: String,
  mods: Modifiers.Value,
  tname: Typename,
  params: Seq[VarStmnt],
  body: Option[Statement]
) extends Definition {
  val isConstructor = name == tname.toString

  def children = params ++ body.toList
}

case class VarStmnt(
  name: String,
  mods: Modifiers.Value,
  tname: Typename,
  value: Option[Expression]
) extends Statement {
  def children = value.toList
}

case class IfStmnt(
  cond: Expression,
  then: Statement,
  otherwise: Option[Statement]
) extends Statement {
  def children = Seq(cond, then) ++ otherwise.toList
}

case class WhileStmnt(
  cond: Expression,
  body: Statement
) extends Statement {
  def children = Seq(cond, body)
}

case class ForStmnt(
  first: Option[Statement],
  cond: Option[Expression],
  after: Option[Expression],
  body: Statement
) extends Statement {
  def children = Seq(body) ++ first.toList ++ cond.toList ++ after.toList
}

case class BlockStmnt(
  body: Seq[Statement]
) extends Statement {
  def children = body
}

case class ReturnStmnt(
  value: Expression
) extends Statement {
  def children = Seq(value)
}

case class ExprStmnt(
  expr: Expression
) extends Statement {
  def children = Seq(expr)
}

case class IntVal(
  value: Int
) extends Expression {
  def children = Seq()
}

case class BoolVal(
  value: Boolean
) extends Expression {
  def children = Seq()
}

case class ThisVal()
extends Expression {
  def children = Seq()
}

case class SuperVal()
extends Expression {
  def children = Seq()
}

case class NullVal()
extends Expression {
  def children = Seq()
}

case class Id(
  name: String
) extends Expression {
  def children = Seq()
}

case class Call(
  method: Expression,
  args: Seq[Expression]
) extends Expression {
  def children = args :+ method
}

case class Cast(
  tname: Typename,
  value: Expression
) extends Expression {
  def children = Seq(value)
}

case class NewType(
  tname: Typename,
  args: Seq[Expression]
) extends Expression {
  def children = args
}

case class NewArray(
  tname: Typename,
  size: Expression
) extends Expression {
  def children = Seq(size)
}

case class CharVal(
  value: Char
) extends Expression {
  def children = Seq()
}

case class StringVal(
  value: String
) extends Expression {
  def children = Seq()
}

case class Assignment(lhs: Expression, rhs: Expression)
extends BinaryOperator

case class LogicOr(lhs: Expression, rhs: Expression)
extends BinaryOperator

case class LogicAnd(lhs: Expression, rhs: Expression)
extends BinaryOperator

case class BitOr(lhs: Expression, rhs: Expression)
extends BinaryOperator

case class BitAnd(lhs: Expression, rhs: Expression)
extends BinaryOperator

case class Eq(lhs: Expression, rhs: Expression)
extends BinaryOperator

case class NEq(lhs: Expression, rhs: Expression)
extends BinaryOperator

case class LEq(lhs: Expression, rhs: Expression)
extends BinaryOperator

case class GEq(lhs: Expression, rhs: Expression)
extends BinaryOperator

case class LThan(lhs: Expression, rhs: Expression)
extends BinaryOperator

case class GThan(lhs: Expression, rhs: Expression)
extends BinaryOperator

case class InstanceOf(lhs: Expression, rhs: Expression)
extends BinaryOperator

case class Add(lhs: Expression, rhs: Expression)
extends BinaryOperator

case class Sub(lhs: Expression, rhs: Expression)
extends BinaryOperator

case class Mul(lhs: Expression, rhs: Expression)
extends BinaryOperator

case class Div(lhs: Expression, rhs: Expression)
extends BinaryOperator

case class Mod(lhs: Expression, rhs: Expression)
extends BinaryOperator

case class Index(lhs: Expression, rhs: Expression)
extends BinaryOperator

case class Member(lhs: Expression, rhs: Expression)
extends BinaryOperator

case class Not(ghs: Expression) extends UnaryOperator

