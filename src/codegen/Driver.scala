package juicy.codegen

import java.io._
import juicy.codegen.Implicits._
import juicy.source.ast._
import juicy.source.PackageTree
import juicy.utils.visitor._

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
    defns.foreach { defn =>
      defn.classId = classId

      val vtableEntry =
        defn match {
          case c: ClassDefn if !c.isInterface =>
            Runtime.setClass(c)

            val label = c.vtableLabel
            Target.global.reference(label)
            label.toString

          case p: PrimitiveDefn =>
            Runtime.setPrimitive(p)
            "0"

          // TODO: we need to generate vtables for arrays
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
    
    val interfaces = defns.filter(c => c.isInterface).map(_.asInstanceOf[ClassDefn])
    val allTypes = defns.flatMap(cls => Seq(cls, cls.arrayOf))
    interfaces.foreach{ int => 
      val label = int.itableLabel
      Target.global.reference(label)
      Target.global.rodata.emit(label)
      allTypes.foreach{ t =>
        if (t implements int) {
          Target.global.rodata.emit(s"dd ${t itableFor int}")
        } else {
          Target.global.rodata.emit(s"dd 0")
        }
      }
    }
    

    // Generate string tables
    strings.foreach { case (str, label) =>
      val charArrayL = AnonLabel("charArray")
      Target.global.rodata.emit(
        label,
        s"dd ${Runtime.string}",
        s"dd $charArrayL",
        charArrayL,
        s"dd ${Runtime.charArray}",
        s"dd ${str.length}"
        )

      if (str.length > 0)
        Target.global.rodata.emit(s"dd ${str.map(_.toInt).mkString(", ")}")
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
