package juicy.source

import juicy.source.ast._
import juicy.utils.Implicits._

case class PackageTree(rawPkgs: Seq[QName], classes: Map[QName, ClassDefn]) {
  private def prefixes(pkg: QName): Seq[QName] = {
    (Seq[QName](Seq()) /: pkg){ (acc, next) =>
      acc :+ (acc.last :+ next)
    }.tail
  }

  // Flat list of all packages
  val pkgs = rawPkgs.flatMap(prefixes).distinct

  val tree: Map[QName, Option[ClassDefn]] =
    pkgs.map(_ -> None).toMap ++
    classes.map(c => c._1 -> Some(c._2))

  def getPackage(pkg: QName): Map[QName, ClassDefn] = {
    val len = pkg.length
    tree
      .filter(_._1.take(len) == pkg)  // same prefix
      .filter(_._1.length == len + 1) // directly inside
      .filter(_._2.isDefined)         // not a package
      .map { case (path, classDef) =>
        path.drop(len) -> classDef.get
      }
  }

  def isPackage(qname: QName): Boolean = {
    tree.get(qname) match {
      case Some(None) => true
      case _          => false
    }
  }

  def getType(qname: QName): Option[ClassDefn] = {
    tree.get(qname) match {
      case Some(Some(c)) => Some(c)
      case _             => None
    }
  }

  // A tree is valid if its size is equal to the sum of its components
  // Otherwise, there must be an intersection between pkgs and classes
  val valid =
    tree.toSeq.length == pkgs.length + classes.toSeq.length
}

