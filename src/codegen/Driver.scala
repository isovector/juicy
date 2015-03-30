package juicy.codegen

import java.io._
import juicy.codegen.Implicits._
import juicy.source.ast.{ClassDefn, PrimitiveDefn, FileNode}
import juicy.source.PackageTree

object Driver {
  def writeFile(path: String, contents: String) {
    Some(new PrintWriter(path)).foreach{ p =>
      p.write(contents)
      p.close
    }
  }

  def apply(pkgtree: PackageTree, files: Seq[FileNode]) = {
    val defns =
      files
        .flatMap(_.classes) ++
      pkgtree
        .tree
        .map(_._2)
        .flatten
        .filter(_.isInstanceOf[juicy.source.ast.PrimitiveDefn])

    // generate vtable and update classIds simultaneously
    Target.global.rodata.emit(Generator.globalVtable)
    var classId = 0
    defns.foreach { defn =>
      defn.classId = classId

      val vtableEntry =
        defn match {
          case c: ClassDefn if !c.isInterface =>
            val label = c.vtableLabel
            Target.global.reference(label)
            label.toString

          // only classes have vtable entries
          case _ =>
            "0"
        }
      Target.global.rodata.emit(
        s"dd $vtableEntry",
        // Put one in for arrays
        "dd 0"
      )

      classId += 2
    }

    files
      .filter(_.classes.length > 0)
      .sortBy(_.classes(0).classId)
      .foreach { f =>
        val fname = s"asm/${f.classes(0).name}.s"
        Target.withFile(fname) {
          f.classes.foreach { c =>
            Generator.emit(c)

            writeFile(fname, Target.file.emitted)
          }
        }
      }

    writeFile("asm/global.s", Target.global.emitted)
    writeFile("asm/types.cc", Target.debug.toString)
  }
}
