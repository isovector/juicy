package juicy.source.resolver

import juicy.source.ast._
import juicy.source.PackageTree
import juicy.source.tokenizer.SourceLocation
import juicy.utils.CompilerError
import juicy.utils.Implicits._
import juicy.utils.visitor._

object Resolver {
  case class OverlappingTypeError(qname: QName, from: SourceLocation)
      extends CompilerError {
    val msg = "Type `" + qname.mkString(".") +  "` overlaps with other types."
  }

  case class UnresolvedTypeError(qname: QName, from: SourceLocation)
      extends CompilerError {
    val msg = "Unresolved type `" + qname.mkString(".") +  "`"
  }

  case class UnknownPackageError(pkg: QName, from: SourceLocation)
      extends CompilerError {
    val msg = "Unknown package `" + pkg.mkString(".") +  "`"
  }

  case class AmbiguousResolveError(qname: QName, from: SourceLocation)
      extends CompilerError {
    val msg = "Unqualified type `" + qname.mkString(".") +  "` resolves ambiguously."
  }

  case class OverlappingPackagesError()
      extends CompilerError {
    val from = SourceLocation("<compiled files>", 0, 0)
    val msg = "Some objects in the package tree have the same qualified name." +
              "This is likely due to having a class whose qualified name is a prefix of a package."
  }
  
  case class AmbiguousPackageClassError(pkg: String, from: SourceLocation)
      extends CompilerError {
    val msg = s"Overlapping definition of class and package with name $pkg"  
      
 }

  def qualify(name: String, context: Seq[Visitable]): QName = {
    context.reverse.flatMap { element =>
      element match {
        case c: ClassDefn => Seq(c.name)
        case f: FileNode  => f.pkg
        case _            => Seq()
      }
    } :+ name
  }

  def apply(nodes: Seq[FileNode]): PackageTree = {
    val types = new collection.mutable.HashMap[QName, ClassDefn]
    val packages =
      new collection.mutable.HashMap[QName,
        collection.mutable.MutableList[QName]]

    def createPkg(pkg: QName) =
      if (!packages.contains(pkg))
        packages += pkg -> new collection.mutable.MutableList[QName]()

    def defaultType(name: String) =
      types += Seq(name) -> ClassDefn(
        name, Modifiers.PUBLIC, Seq(), Seq(), Seq(), Seq())

    defaultType("int")
    defaultType("char")
    defaultType("boolean")
    defaultType("short")
    defaultType("byte")
    defaultType("void")

    createPkg(Seq("java", "lang"))

    // Add all new fully qualified types to a big dictionary
    nodes.foreach { node =>
      val pkg = node.pkg
      createPkg(pkg)

      node.classes.foreach { classDef =>
        val qname = pkg :+ classDef.name
        if (!types.contains(qname))
          types += qname -> classDef
        else
          throw OverlappingTypeError(qname, classDef.from)
        packages(pkg) += qname
      }
    }
    
    // Build the package tree
    val pkgtree = PackageTree(
      packages.toSeq.map(_._1),
      types.toMap)

    if (!pkgtree.valid)
      throw OverlappingPackagesError()

    nodes.foreach { node =>
      val importedTypes = new collection.mutable.HashMap[QName, ClassDefn]
      val importedPkgs = new collection.mutable.MutableList[QName]

      // TODO: primitives
      importedPkgs += Seq("java", "lang")

      node.imports.foreach {
        case impl@ImportClass(tname) =>
          val qname = tname.qname
          val resolved = pkgtree.getType(qname)
          val name = Seq(qname.last)

          if (resolved.isDefined)
            if (
                !importedTypes.contains(name) ||
                importedTypes(name) == resolved.get)
              importedTypes += name -> resolved.get
            else throw OverlappingTypeError(qname, impl.from)
          else
            throw UnresolvedTypeError(qname, impl.from)

        case impl@ImportPkg(qname) =>
          if (!pkgtree.tree.contains(qname))
            throw UnknownPackageError(qname, impl.from)
          if (!importedPkgs.contains(qname))
            importedPkgs += qname
      }

      val pkg = node.pkg
      
      def tryResolve(qname: QName, from: SourceLocation): Option[ClassDefn] = {
        var outVal =
          if (types.contains(qname))
            types.get(qname)
          else if (importedTypes.contains(qname))
            importedTypes.get(qname)
          else
            None
      
        def tryResolveFromPackage(pkg: QName) = {
          val pkgContents = pkgtree.getPackage(pkg)
          val contained = pkgContents.get(qname)
          if (contained.isDefined) {
            if (outVal.isDefined && outVal != contained)
              throw AmbiguousResolveError(qname, from)
            outVal = contained
          }
        }
        if (outVal.isEmpty)
          tryResolveFromPackage(pkg)
        if (outVal.isEmpty)
          importedPkgs.foreach(tryResolveFromPackage)
        return outVal
      }
      
      node.visit { (self, context) =>
        implicit val implContext = context
        self match {
          case Before(classDefn: ClassDefn) =>
            val qname = Seq(classDefn.name)
            if (importedTypes.contains(qname) &&
                importedTypes(qname) != classDefn) {
              println("FROM HERE")
              throw AmbiguousResolveError(qname, classDefn.from)
            }
          case Before(tname@Typename(qname, _)) =>
            if (!isIn[ImportPkg]() && !isIn[ImportClass]()) {
              val prefixNames = tname.name.split('.').toList
              (1 to prefixNames.length - 1) foreach { prefix =>
                val resolved = tryResolve(prefixNames.take(prefix), tname.from)
                if (resolved.isDefined) {
                  println("OR HERE")
                  throw AmbiguousResolveError(prefixNames.take(prefix), tname.from)
                }
              }
            }
            tname.resolved = tryResolve(qname, tname.from)
            if (!tname.resolved.isDefined)
              throw UnresolvedTypeError(qname, tname.from)
          case _ =>
        }
      }.fold(
        l => throw VisitError(l),
        r => r
      )
    }

    pkgtree
  }
}


