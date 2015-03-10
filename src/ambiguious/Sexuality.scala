package juicy.source.ambiguous

import juicy.source.ast._
import juicy.utils.Implicits._
import juicy.utils.visitor._

object AmbiguousStatus {
  type Value = Int

  val AMBIGUOUS = 0
  val SCOPE     = 1
  val PACKAGE   = 2
  val TYPE      = 3
}

object Sexuality {
  import AmbiguousStatus._

  def apply(nodes: Seq[FileNode]): Seq[FileNode] = {
    nodes.map { node =>
      val typeScope = node.typeScope

      // the most balls function of all time
      def unambiguousType(name: String): Option[ClassDefn] = {
        val possible = typeScope.get.get(name).getOrElse { return None }
        val classImport = possible.find(!_.fromPkg)

        if (classImport.isDefined)
          classImport.map(_.u)
        else if (possible.length == 1)
          Some(possible(0))
        else // TODO: throw exception
          None
      }

      node.rewrite(Rewriter { (node: Visitable, context: Seq[Visitable]) =>
        implicit val implContext = context
        node match {

    case id: Id =>
      if (isIn[Member](_.lhs == id)) {
        val name = id.name
        val asType = unambiguousType(name)
        id.status =
          if (id.scope.resolve(name).isDefined)
            SCOPE
          else if (asType.isDefined)
            TYPE
          else
            PACKAGE
      }

      id


    case m: Member =>
      val folded =
        m.fold {
          case id: Id     => Some(id)
          case otherwise  => None
        }

      if (!folded.contains(None)) {
        val path = folded.flatten
        if (path.last.status == TYPE) {
          m
        } else m
      } else m

          case otherwise => otherwise
        }
      }).asInstanceOf[FileNode]
    }
  }
}

