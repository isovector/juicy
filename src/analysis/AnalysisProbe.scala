package juicy.source.analysis;

import juicy.source.ast._
import juicy.source.ast.Modifiers._
import juicy.utils.CompilerError
import juicy.utils.Implicits._
import juicy.utils.visitor._

object AnalysisProbe {
  case class UninitVarError(id: Id) extends CompilerError {
    val from = id.from
    private val name = id.name
    val msg = s"Variable `$id` used before being defined"
  }

  case class UnreachableError(stmnt: Statement) extends CompilerError {
    val from = stmnt.from
    val msg = "Statement is unreachable"
  }

  case class MethodReturnError(method: MethodDefn) extends CompilerError {
    val from = method.from
    private val name = method.name
    private val qname = method.tname
    val msg = s"Method `$qname $name` has a code-path which doesn't return"
  }

  private def check(which: Modifiers.Value, flag: Modifiers.Value) =
    (which & flag) == flag

  def apply(node: FileNode) = {
    val fields =
      node
        .classes
        .flatMap(_.fields)

    probeFields(
      fields
        .filter(f => check(f.mods, STATIC)))

    probeFields(
      fields
        .filter(f => !check(f.mods, STATIC)))

    node
      .classes
      .flatMap(_.methods)
      .filter(_.body.isDefined)
      .foreach { method =>
        val result = probe(method.body.get)
        if (method.tname.qname != Seq("void") && result && !method.isCxr)
          throw MethodReturnError(method)
      }
  }

  def foreachVarDefn(n: Visitable)(handle: Id => Unit) = {
    n.visit { (node, context) =>
      implicit val implContext = context
      before(node) match {
        case id@Id(name) =>
          context.headOption match {
            case Some(Member(_, rhs)) if id == rhs =>
            case Some(_: StaticMember )            =>
            case Some(_: Assignee)                 =>
            case Some(_: Callee)                   =>
            case _                                 =>
              handle(id)
          }

        case _ =>
      }
    } .fold(
      l => throw VisitError(l),
      r => { }
    )
  }

  def probeFields(fields: Seq[VarStmnt]) = {
    (Set[String]() /: fields) { (inScope, field) =>
      field
        .value
        .map(n => foreachVarDefn(n) { id =>
          val name = id.name
          if (!inScope.contains(name))
            throw UninitVarError(id)
        }
      )

      inScope + field.name
    }
  }

  def probe(stmnt: Statement): Boolean = probe(true, stmnt)

  private def probe(reachable: Boolean, stmnt: Statement): Boolean = {
    if (!reachable)
      throw UnreachableError(stmnt)

    stmnt match {
      case BlockStmnt(stmnts) =>
        (true /: stmnts)(probe)

      case IfStmnt(_, then, otherwise) =>
        if (otherwise.isDefined)
          probe(then) || probe(otherwise.get)
        else
          // You can always get through a then block
          true

      case ForStmnt(_, cond, _, body) =>
        val const = cond == Some(BoolVal(true)) || cond == None
        // Can have a const expr as long as your probe fails
        probe(body) && !const

      case WhileStmnt(cond, body) =>
        val const = cond == BoolVal(true)
        // Can have a const expr as long as your probe fails
        probe(body) && !const

      case VarStmnt(name, _, _, value) =>
        if (value.isDefined)
          foreachVarDefn(value.get) { id =>
            if (!id.scope.get.definedBefore(id.name, name))
              throw UninitVarError(id)
          }
        true

      case _: ReturnStmnt =>
        false

      case _: Statement =>
        true
    }
  }
}
