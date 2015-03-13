package juicy.source.analysis;

import juicy.source.ast._
import juicy.utils.CompilerError
import juicy.utils.Implicits._

object AnalysisProbe {
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

  def apply(node: FileNode) = {
    // TODO: ensure each method probe is false
    node
      .classes
      .flatMap(_.methods)
      .filter(_.body.isDefined)
      .foreach { method =>
        val result = probe(method.body.get)
        if (method.tname.qname != Seq("void") && result)
          throw MethodReturnError(method)
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
        probe(body) --> !const

      case WhileStmnt(cond, body) =>
        val const = cond == BoolVal(true)
        // Can have a const expr as long as your probe fails
        probe(body) --> !const

      case _: ReturnStmnt =>
        false

      case o: Statement =>
        true
    }
  }
}

