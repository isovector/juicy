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

  // A tree is valid if its size is equal to the sum of its components
  // Otherwise, there must be an intersection between pkgs and classes
  val valid =
    tree.toSeq.length == pkgs.length + classes.toSeq.length
}

