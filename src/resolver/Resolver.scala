package juicy.source.resolver

import juicy.source.ast._
import juicy.utils.Implicits._
import juicy.utils.visitor._

object Resolver {
  case class UnresolvedTypeException(qname: QName) extends Throwable
  case class UnknownPackageException(pkg: QName) extends Throwable

  def qualify(name: String, context: Seq[Visitable]): QName = {
    context.reverse.flatMap { element =>
      element match {
        case ClassDefn(name, _, _, _, _, _, _, _) => Seq(name)
        case FileNode(pkg, _, _)                  => pkg
        case _                                    => Seq()
      }
    } :+ name
  }

  def apply(nodes: Seq[FileNode]) = {
    val types = new collection.mutable.HashMap[QName, ClassDefn]
    val packages =
      new collection.mutable.HashMap[QName,
        collection.mutable.MutableList[QName]]

    // Add all new fully qualified types to a big dictionary
    nodes.map { node =>
      val pkg = node.pkg

      if (!packages.contains(pkg))
        packages += pkg -> new collection.mutable.MutableList[QName]()

      node.visit((_: Unit, _: Unit) => {})
      { (self, context) =>
        self match {
          case Before(classDef@ClassDefn(name, _, _, _, _, _, _, _)) =>
            val qname = qualify(name, context)
            types += qname -> classDef.asInstanceOf[ClassDefn]
            packages(pkg) += qname
          case _ =>
        }
      }
    }

    // Resolve typenames to the classes above
    nodes.map { node =>
      // Build the import list
      val importTypes =
        new collection.mutable.HashMap[QName, QName]

      def importPkg(pkg: QName) = {
        if (!packages.contains(pkg))
          throw new UnknownPackageException(pkg)

        packages(pkg).map { classInPkg =>
          importTypes += Seq(classInPkg.last) -> classInPkg
        }
      }

      if (node.pkg != Seq())
        importPkg(node.pkg)

      node.imports.map {
        case ImportClass(tname) =>
          importTypes += Seq(tname.qname.last) -> tname.qname
        case ImportPkg(pkg)     => importPkg(pkg)
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
          else throw new UnresolvedTypeException(qname)
      }

      node.visit((_: Unit, _: Unit) => {})
      { (self, context) =>
        self match {
          case Before(n) =>
            n match {
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

          case _ =>
        }
      }
    }
  }
}

