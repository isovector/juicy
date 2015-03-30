package juicy.codegen

import java.io._
import juicy.codegen.Implicits._
import juicy.source.ast._
import juicy.source.PackageTree
import juicy.utils.visitor._
import org.apache.commons.lang3.{StringEscapeUtils => Escape}

object Driver {
  def writeFile(path: String, contents: String) {
    Some(new PrintWriter(path)).foreach{ p =>
      p.write(contents)
      p.close
    }
  }

  def apply(pkgtree: PackageTree, files: Seq[FileNode]) = {
    val strings = collection.mutable.Map[String, Label]()

    // Give each string a unique string id
    files.foreach { f =>
      f.visit { (node, context) =>
        node match {
          case Before(s@StringVal(str)) =>
            if (!(strings contains str)) {
              val label = GlobalAnonLabel("string")
              strings += str -> label
              Target.global.export(label)
            }

            s.interned = strings(str)

          case _ =>
        }
      }
    }

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
    var stringId = -1
    var charArrayId = -1
    defns.foreach { defn =>
      defn.classId = classId

      val vtableEntry =
        defn match {
          case c: ClassDefn if !c.isInterface =>
            if (c.name == "String" && c.pkg == Seq("java", "lang"))
              stringId = classId

            val label = c.vtableLabel
            Target.global.reference(label)
            label.toString

          case p: PrimitiveDefn =>
            if (p.name == "char")
              charArrayId = classId + 1
            "0"

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


    // Generate string tables
    strings.foreach { case (str, label) =>
      val charArrayL = AnonLabel("charArray")
      Target.global.rodata.emit(
        label,
        s"dd $stringId",
        s"dd $charArrayL",
        charArrayL,
        s"dd $charArrayId",
        s"dd ${str.length}"
        )
      str.foreach { c =>
        Target.global.rodata.emit( s"dd '${Escape.escapeJava(c.toString)}'")
      }
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
