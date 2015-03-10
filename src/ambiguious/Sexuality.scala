package juicy.source.ambiguous

import juicy.source.ast._
import juicy.utils.visitor.Rewriter

object Sexuality {
  def apply(nodes: Seq[FileNode]): Seq[FileNode] = {
    nodes.map { node =>
      node.rewrite(Rewriter {

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
      }).asInstanceOf[FileNode]
    }
  }
}

