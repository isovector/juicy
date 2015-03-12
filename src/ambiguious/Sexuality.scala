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
      val typeScope = node.typeScope

      // the most balls function of all time
      def unambiguousType(name: String, from: SourceLocation): Option[TypeDefn] = {
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
        val asType = node.resolve(prefix :+ name, pkgtree, id.from)

        id.status =
          if (id.scope.get.resolve(name).isDefined)
            SCOPE
          else if (asType.isDefined)
            TYPE
          else
            PACKAGE
      }

      node.rewrite(Rewriter { (newNode: Visitable, context: Seq[Visitable]) =>
        implicit val implContext = context
        newNode match {

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
        node.unambiguousType(qname.last, m.from)
        if (classDefn.isEmpty) {
          println(qname.mkString("."))
        }
        classDefn.get match {
           case cd: ClassDefn => StaticMember(cd, rhs)
           case t: TypeDefn => throw PrimitiveReferenceError(t, m.from)
        }
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

