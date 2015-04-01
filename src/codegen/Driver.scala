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
          case c: ClassDefn =>
            Runtime.setClass(c)

            if (!c.isInterface) {
              val label = c.vtableLabel
              Target.global.reference(label)
              label.toString
            } else "0"

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


    Target.global.export(Generator.gStaticInit)
    Target.global.rodata.emit(Generator.gStaticInit)
    Target.global.rodata.emit(Prologue())
    defns
      .filter(_.isInstanceOf[ClassDefn])
      .map(_.asInstanceOf[ClassDefn])
      .filter(!_.isInterface)
      .foreach { defn =>
        Target.global.reference(defn.staticInitLabel)
        Target.global.rodata.emit(s"call ${defn.staticInitLabel}")
      }
    Target.global.rodata.emit(Epilogue())


    Target.global.rodata.emit(Generator.hierarchyTable)
    defns.foreach { defn =>
      defn match {
        case c: ClassDefn if !c.isInterface =>
          Target.global.reference(c.hierarchyLabel)
          Target.global.reference(c.arrayOf.hierarchyLabel)
          Target.global.rodata.emit(s"dd ${c.hierarchyLabel}; hierarchy for ${c.name}")
        case t: TypeDefn =>
          Target.global.rodata.emit(s"dd 0; no hierarchy for ${t.name}")
          Target.global.export(defn.arrayOf.hierarchyLabel)
      }
      Target.global.rodata.emit(s"dd ${defn.arrayOf.hierarchyLabel}; hierarchy for ${defn.arrayOf.name}")
    }

    defns.foreach { defn =>
      defn match {
        case c: ClassDefn if !c.isInterface =>
        case t: TypeDefn =>
          val arrT = t.arrayOf
          Target.global.rodata.emit(arrT.hierarchyLabel)
          (arrT +: arrT.superTypes).distinct.map( sup =>
            Target.global.rodata.emit(s"dd ${Runtime.lookup(sup)}; hierarchy for ${sup.name}")
          )
          Target.global.rodata.emit(s"dd -1; end of hierarchy")
      }
    }

    val interfaces = defns.filter(c => c.isInterface).map(_.asInstanceOf[ClassDefn])
    interfaces.foreach{ int =>
      val label = int.itableLabel
      Target.global.export(label)
      Target.global.rodata.emit(label)
      defns.foreach{ t =>
        if (t implements int) {
          Target.global.reference(t itableFor int)
          Target.global.rodata.emit(s"dd ${t itableFor int}")
        } else {
          Target.global.rodata.emit(s"dd 0")
        }
        if (ArrayDefn.sharedImpls.find(_.r resolvesTo int) != None) {
          Target.global.export(ArrayDefn itableFor int)
          Target.global.rodata.emit(s"dd ${ArrayDefn itableFor int}")
        } else {
          Target.global.rodata.emit(s"dd 0")
        }
      }
    }

    val objectType = pkgtree.getType(Seq("java", "lang", "Object")).get
    ArrayDefn.sharedImpls.map(_.r.asInstanceOf[ClassDefn]).foreach { interface =>
      Target.global.rodata.emit(ArrayDefn itableFor interface)
      interface.allMethods.map(_.signature).foreach { sig =>
        val methLabel = objectType.methods.find(_.signature == sig).get.label
        Target.global.reference(methLabel)
        Target.global.rodata.emit(s"dd $methLabel")
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
