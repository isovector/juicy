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

object AST {
  case class ClassDefn(
    name: String,
    mods: Modifiers.Value,
    impls: Seq[String],
    extnds: Seq[String],
    fields: Seq[FieldStmnt],
    methods: Seq[MethodDefn]
  ) extends Definition {
    def children = fields ++ methods
  }

  case class FieldStmnt(
    name: String,
    mods: Modifiers.Value,
    tname: String,
    value: Option[Expression]
  ) extends Statement {
    def children = value.toList
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
    first: Statement,
    cond: Expression,
    after: Expression,
    body: Statement
  ) extends Statement {
    def children = Seq(first, cond, after, body)
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
}

