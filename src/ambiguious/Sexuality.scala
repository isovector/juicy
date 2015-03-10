package juicy.source.ambiguous

import juicy.source.ast._
import juicy.utils.visitor._

object Sexuality {
  def apply(nodes: Seq[FileNode]): Seq[FileNode] = {
    nodes.map { node =>
      node.rewrite(Rewriter { (node: Visitable, context: Seq[Visitable]) =>
        implicit val implContext = context
        node match {

    case id: Id =>
      if (isIn[Member](_.lhs == id)) {
        println(id.name)
      }
      id

    case m: Member =>
      val folded =
        m.fold {
          case id: Id     => Some(id)
          case otherwise  => None
        }

      if (!folded.contains(None)) {
        val possibleTname = folded.flatten.map(_.name)
        println(possibleTname.mkString("//"))
        m
      } else m

          case otherwise => otherwise
        }
      }).asInstanceOf[FileNode]
    }
  }
}

