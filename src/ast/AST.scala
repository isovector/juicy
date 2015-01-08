package juicy.ast


trait Node {
  def children: Seq[Node]

  def visit[T]
      (fold: (T, T) => T)
      (before: => T)
      (after: => T): T = {
    fold(
      (before /:
        children.map(
          _.visit(fold)(before)(after)
        )
      )(fold), after)
  }
}

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

trait Expression extends Node
trait Statement extends Node
trait Definition extends Node

trait BinaryOperator extends Expression {
  val lhs: Expression
  val rhs: Expression

  def children = Seq(lhs, rhs)
}


object AST {
  case class ClassDefn(
    name: String,
    mods: Modifiers.Value,
    extnds: Option[String],
    impls: Seq[String],
    fields: Seq[VarStmnt],
    methods: Seq[MethodDefn]
  ) extends Definition {
    def children = fields ++ methods
  }

  case class MethodDefn(
    name: String,
    mods: Modifiers.Value,
    tname: String,
    args: Seq[VarStmnt],
    body: Statement
  ) extends Definition {
    def children = args :+ body
  }

  case class VarStmnt(
    name: String,
    mods: Modifiers.Value,
    tname: String,
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

  case class ExprStmnt(
    expr: Expression
  ) extends Statement {
    def children = Seq(expr)
  }

  case class ConstIntExpr(
    value: Int
  ) extends Expression {
    def children = Seq()
  }

  case class ConstBoolExpr(
    value: Boolean
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
}

