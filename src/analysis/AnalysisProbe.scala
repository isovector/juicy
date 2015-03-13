package juicy.source.analysis;

import juicy.source.ast._
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

  def apply(node: FileNode) = {
    node
      .classes
      .flatMap(_.methods)
      .filter(_.body.isDefined)
      .foreach { method =>
        val result = probe(method.body.get, Set())._1
        if (method.tname.qname != Seq("void") && result)
          throw MethodReturnError(method)
      }
  }

  def probe(stmnt: Statement, uninit: Set[String]): (Boolean, Set[String]) =
    probe(true, stmnt, uninit)

  private def probe(
      reachable: Boolean,
      stmnt: Statement,
      uninitVars: Set[String]): (Boolean, Set[String]) = {
    if (!reachable)
      throw UnreachableError(stmnt)

    // Only visit this node's expression children
    var definedVars = Set[String]()
    stmnt
      .children
      .filter(_.isInstanceOf[Expression])
      .foreach(_.visit { (node, context) =>
        implicit val implContext = context
        node match {
          case Before(id: Id) =>
            if (!(isIn[Assignee]()))
              if (id.isVar && uninitVars.contains(id.name))
                throw UninitVarError(id)

          case After(Assignment(Assignee(lhs: Id), rhs)) if rhs != NullVal() =>
            if (!isIn[BlockStmnt]() && lhs.isVar) {
              println("defined",lhs,rhs)
              definedVars += lhs.name
            }

          case _ =>
        }
      }.fold(
        l => throw VisitError(l),
        r => {}))

    val uninit = uninitVars -- definedVars

    stmnt match {
      case BlockStmnt(stmnts) =>
        ((true, uninit) /: stmnts) {
          case ((reach, init), stmnt) => probe(reach, stmnt, init) }

      case IfStmnt(_, then, otherwise) =>
        val (treach, tinit) = probe(then, uninit)

        if (otherwise.isDefined) {
          val (oreach, oinit) = probe(otherwise.get, uninit)
          println(tinit.mkString(":"), oinit.mkString(":"))
          (treach || oreach, tinit ++ oinit)
        } else
          (true, tinit)

      case ForStmnt(_, cond, _, body) =>
        val const = cond == Some(BoolVal(true)) || cond == None
        val (reach, init) = probe(body, uninit)
        // Can have a const expr as long as your probe fails
        (reach --> !const, init)

      case WhileStmnt(cond, body) =>
        val const = cond == BoolVal(true)
        val (reach, init) = probe(body, uninit)
        // Can have a const expr as long as your probe fails
        (reach --> !const, init)

      case _: ReturnStmnt =>
        (false, uninit)

      case VarStmnt(name, _, _, value) =>
        value match {
          case Some(NullVal()) =>
            (true, uninit + name)

          case Some(id@Id(iname)) if name == iname =>
            throw UninitVarError(id)

          case _ => (true, uninit)
        }

      case o: Statement =>
        (true, uninit)
    }
  }
}

