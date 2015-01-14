package juicy.source.resolver

import juicy.source.ast._
import juicy.source.ast.AST._

object Resolver {
  def qualify(name: String, context: Seq[Node]): Seq[String] = {
    Seq(name) ++ context.flatMap { element =>
      element match {
        case ClassDefn(name, _, _, _, _, _, _, _) => Seq(name)
        case FileNode(pkg, _, _)                  => pkg
        case _                                    => Seq()
      }
    }
  }

  def apply(nodes: Seq[FileNode]) = {
    val types = new scala.collection.mutable.HashMap[Seq[String], ClassDefn]

    // Add all new fully qualified types to a big dictionary
    nodes.map { node =>
      node.visit((_: Unit, _: Unit) => {})
      { (self: Node, context: Seq[Node]) =>
        self match {
          case ClassDefn(name, _, _, _, _, _, _, _) =>
            val qname = qualify(name, context)
            types += qname -> self.asInstanceOf[ClassDefn]
          case _ =>
        }
      } ((_, _) => {})
    }

    // Resolve typenames to the classes above
    nodes.map { node =>
      // Build the import list
      val importPaths = new scala.collection.mutable.MutableList[Seq[String]]
      val importTypes =
        new scala.collection.mutable.HashMap[Seq[String], Seq[String]]

      if (node.pkg != Seq())
        importPaths += node.pkg

      node.imports.map {
        case ImportClass(tname) =>
          importTypes += Seq(tname.qname.head) -> tname.qname
        case ImportPkg(qname)   => importPaths += qname
        case _                  =>
      }

      // Change a tname's resolved var to the class it describes
      def resolve(tname: Typename): Unit = {
        if (tname.resolved.isDefined) return;
        val qname = tname.qname

        tname.resolved =
          if (types.contains(qname))
            Some(types(qname))
          else if (importTypes.contains(qname))
            Some(types(importTypes(qname)))
          else
            importPaths.map { path =>
              types.get(qname ++ path)
            }.find(_.isDefined).getOrElse(None)
      }

      node.visit((_: Unit, _: Unit) => {})
      { (self: Node, context: Seq[Node]) =>
        self match {
          case ClassDefn(_, _, extnds, impls, _, _, _, _) =>
            extnds.map(resolve)
            impls.map(resolve)

          case MethodDefn(_, _, tname, _, _) => resolve(tname)
          case VarStmnt(_, _, tname, _)      => resolve(tname)
          case Cast(tname, _)                => resolve(tname)
          case NewType(tname, _)             => resolve(tname)
          case NewArray(tname, _)            => resolve(tname)
          case _                             =>
        }
      } ((_, _) => {})
    }

  }
}

