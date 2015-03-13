package juicy.source.ambiguous

import juicy.source._
import juicy.source.ast._
import juicy.source.resolver.Resolver.AmbiguousResolveError
import juicy.source.resolver.Resolver.PrimitiveReferenceError
import juicy.source.tokenizer.SourceLocation
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

  def apply(nodes: Seq[FileNode], pkgtree: PackageTree): Seq[FileNode] = {
    nodes.map { node =>
      def disambiguate(id: Id, prefix: QName): Unit = {
        val name = id.name

        id.status =
          if (id.scope.flatMap(_.resolve(name)).isDefined)
            SCOPE
          else {
            val asType =
              node.resolve(prefix :+ name, pkgtree, id.from)
            if (asType.isDefined)
              TYPE
            else
              PACKAGE
          }
      }

      node.rewrite(Rewriter { (newNode: Visitable, context: Seq[Visitable]) =>
        implicit val implContext = context
        newNode match {

  // ---------------------------------------------------------------------------

  case id: Id =>
    if (isIn[Member](_.lhs == id))
      disambiguate(id, Seq())
    id


  case m: Member =>
    val folded =
      m.fold {
        case id: Id if id.status != SCOPE => Some(id)
        case _ => None
      }

    if (!folded.contains(None)) {
      val totalFolded = folded.flatten
      val rhs = totalFolded.last
      val path = totalFolded.dropRight(1)
      val qname = path.map(_.name)

      if (path.last.status == TYPE) {
        val classDefn = node.resolve(qname, pkgtree, m.from).get

        if (!classDefn.isInstanceOf[ClassDefn])
          throw PrimitiveReferenceError(classDefn, m.from)

        StaticMember(classDefn.asInstanceOf[ClassDefn], rhs)
      } else {
        if (rhs.isInstanceOf[Id])
          disambiguate(rhs.asInstanceOf[Id], qname)
        m
      }
    } else m

  // ---------------------------------------------------------------------------

          case otherwise => otherwise
        }
      }).asInstanceOf[FileNode]
    }
  }
}
