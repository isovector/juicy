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

  val children = Seq(lhs, rhs)
}

trait UnaryOperator extends Expression {
  val ghs: Expression

  val children = Seq(ghs)
}

case class Typename (qname: QName, isArray: Boolean=false) extends Visitable {
  var resolved: Option[ClassDefn] = None
  val name = qname.mkString(".")
  val brackets = if (isArray) " []" else ""
  val isPrimitive =
    Seq("void", "boolean", "int", "short", "byte", "char").contains(name)
  override def toString() = s"$name$brackets"

  val children = Seq()
}

case class FileNode(
  pkg: QName,
  imports: Seq[ImportStmnt],
  classes: Seq[ClassDefn]
) extends Visitable {
  val children = imports ++ classes
}

object ClassDefn {
  private var autoIncr = 0
  private var enableIncr = true

  def getNextEqualityComparitor: Int = {
    if (enableIncr)
      autoIncr += 1
    autoIncr
  }

  def suspendUniqueness[T](c: => T) = {
    enableIncr = false
    c
    enableIncr = true
  }
}

case class ClassDefn(
  name: String,
  mods: Modifiers.Value,
  extnds: Seq[Typename],
  impls: Seq[Typename],
  fields: Seq[VarStmnt],
  methods: Seq[MethodDefn],
  isInterface: Boolean = false
) extends Definition {
  val equalityComparitor = ClassDefn.getNextEqualityComparitor

  val children = extnds ++ impls ++ fields ++ methods

  val isClass = !isInterface

  lazy val (allMethods: Seq[MethodDefn], hidesMethods: Seq[MethodDefn]) = {
    val parentMethods =
      extnds.flatMap(_.resolved.get.allMethods)
    val sigs = methods.map(_.signature)
    val (hides, keeps) = parentMethods.partition { parMeth =>
      sigs.contains(parMeth.signature)
    }

    (methods ++ keeps, hides)
  }

  override def equals(o: Any) = o match {
    case that: ClassDefn =>
       ( name               == that.name
      && mods               == that.mods
      && extnds             == that.extnds
      && impls              == that.impls
      && fields             == that.fields
      && methods            == that.methods
      && isInterface        == that.isInterface
      && equalityComparitor == that.equalityComparitor
       )
    case _               => false
  }

  override def hashCode = name.hashCode + equalityComparitor.hashCode

  override def toString = equalityComparitor.toString
}

case class ImportClass(
  tname: Typename
) extends ImportStmnt {
  val children = Seq(tname)
}

case class ImportPkg(
  pkg: QName
) extends ImportStmnt {
  val children = Seq()
}

case class Signature(name: String, params: Seq[Typename])
case class MethodDefn(
  name: String,
  mods: Modifiers.Value,
  isCxr: Boolean,
  tname: Typename,
  params: Seq[VarStmnt],
  body: Option[Statement]
) extends Definition {
  val children = Seq(tname) ++ params ++ body.toList

  val signature = Signature(name, params.map(_.tname))

  // Equivalency equality
  def ~==(other: MethodDefn): Boolean =
    signature == other.signature
}

case class VarStmnt(
  name: String,
  mods: Modifiers.Value,
  tname: Typename,
  value: Option[Expression]
) extends Statement {
  val children = tname +: value.toList
}

case class IfStmnt(
  cond: Expression,
  then: BlockStmnt,
  otherwise: Option[BlockStmnt]
) extends Statement {
  val children = Seq(cond, then) ++ otherwise.toList
}

case class WhileStmnt(
  cond: Expression,
  body: Statement
) extends Statement {
  val children = Seq(cond, body)
}

case class ForStmnt(
  first: Option[Statement],
  cond: Option[Expression],
  after: Option[Expression],
  body: Statement
) extends Statement {
  val children = first.toList ++ cond.toList ++ after.toList ++ Seq(body)
}

case class BlockStmnt(
  body: Seq[Statement]
) extends Statement {
  val children = body
}

case class ReturnStmnt(
  value: Expression
) extends Statement {
  val children = Seq(value)
}

case class ExprStmnt(
  expr: Expression
) extends Statement {
  val children = Seq(expr)
}

case class IntVal(
  value: Int
) extends Expression {
  val children = Seq()
}

case class BoolVal(
  value: Boolean
) extends Expression {
  val children = Seq()
}

case class ThisVal()
extends Expression {
  val children = Seq()
}

case class SuperVal()
extends Expression {
  val children = Seq()
}

case class NullVal()
extends Expression {
  val children = Seq()
}

case class Id(
  name: String
) extends Expression {
  val children = Seq()
}

case class Call(
  method: Expression,
  args: Seq[Expression]
) extends Expression {
  val children = args :+ method
}

case class Cast(
  tname: Typename,
  value: Expression
) extends Expression {
  val children = Seq(tname, value)
}

case class NewType(
  tname: Typename,
  args: Seq[Expression]
) extends Expression {
  val children = tname +: args
}

case class NewArray(
  tname: Typename,
  size: Expression
) extends Expression {
  val children = Seq(tname, size)
}

case class CharVal(
  value: Char
) extends Expression {
  val children = Seq()
}

case class StringVal(
  value: String
) extends Expression {
  val children = Seq()
}

case class InstanceOf(
  lhs: Expression,
  tname: Typename
) extends Expression {
  val children = Seq(lhs, tname)
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

case class Neg(ghs: Expression) extends UnaryOperator
