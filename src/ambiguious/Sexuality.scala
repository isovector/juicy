package juicy.source.ambiguous

import juicy.source._
import juicy.source.ast._
import juicy.source.resolver.Resolver.AmbiguousResolveError
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
      val typeScope = node.typeScope

      // the most balls function of all time
      def unambiguousType(name: String, from: SourceLocation): Option[ClassDefn] = {
        val possible = typeScope.flatMap(_.get(name)).getOrElse { return None }
        val classImport = possible.find(!_.fromPkg)

        if (classImport.isDefined)
          classImport.map(_.u)
        else if (possible.length == 1)
          Some(possible(0))
        else throw AmbiguousResolveError(Seq(name), from)
      }

      def disambiguate(id: Id, prefix: QName): Unit = {
        val name = id.name
        val asType = unambiguousType(name, id.from) orElse
          pkgtree.getType(prefix :+ name)

        id.status =
          if (id.scope.resolve(name).isDefined)
            SCOPE
          else if (asType.isDefined)
            TYPE
          else
            PACKAGE
      }

      node.rewrite(Rewriter { (node: Visitable, context: Seq[Visitable]) =>
        implicit val implContext = context
        node match {

  // ---------------------------------------------------------------------------

  case id: Id =>
    if (isIn[Member](_.lhs == id)) disambiguate(id, Seq())
    id


  case m: Member =>
    val folded =
      m.fold {
        case id: Id     => Some(id)
        case otherwise  => None
      }

    if (!folded.contains(None)) {
      val totalFolded = folded.flatten
      val rhs = totalFolded.last
      val path = totalFolded.dropRight(1)
      val qname = path.map(_.name)

      if (path.last.status == TYPE) {
        val classDefn = pkgtree.getType(qname) orElse
          unambiguousType(qname.last, m.from)
        StaticMember(classDefn.get, rhs)
      }
      else {
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

