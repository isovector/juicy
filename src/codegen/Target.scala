package juicy.codegen

class Section(which: String, align: Int = 0) extends Emittable {
  private val instructions = collection.mutable.MutableList[Instruction]()

  private def emitImpl(ins: Instruction) = {
    instructions += ins
  }

  def emit(ins: Instruction*): Unit = {
    ins.toList.foreach(emitImpl)
  }

  val alignment =
    if (align != 0)
      s"align=$align"
    else
      ""

  emit(RawInstr(s"section .$which $alignment"))
  def emitted = {
    instructions.map(_.emitted).mkString("\n")
  }
}

object Target {
  var file = new Target
  val global = new Target

  object debug {
    private val defns =
      // typedefs for primitives
      collection.mutable.MutableList[String](
        "typedef int aligned_int __attribute__ ((aligned (4)));",
        "typedef char aligned_byte __attribute__ ((aligned (4)));",
        "typedef char aligned_char __attribute__ ((aligned (4)));",
        "typedef short aligned_short __attribute__ ((aligned (4)));",
        "typedef bool aligned_boolean __attribute__ ((aligned (4)));"
      )

    def add(t: juicy.source.ast.ClassDefn) = {
      defns += t.debugTypeLayout
    }

    override def toString = defns.mkString("\n")
  }

  def text = file.text
  def data = file.data
  def rodata = file.rodata

  def withFile[T](fileName: String)(doWhat: => T) = {
    file = new Target
    doWhat
    // TODO: write file to fileName
  }

  def fromGlobal(ls: Label*) = {
    ls.toList.foreach { l =>
      global.export(l)
      file.reference(l)
    }
  }
}

class Target extends Emittable {
  private val imports = collection.mutable.Set[Label](
    NamedLabel("_malloc"),
    NamedLabel("_exception"))
  private val exports = collection.mutable.Set[Label]()

  def export(l: Label) = {
    exports += l
  }

  def reference(l: Label) = {
    imports += l
  }

  val text = new Section("text")
  val data = new Section("data", 4)
  val rodata = new Section("rodata", 4)

  def emitted = {
    exports
      .map("global " + _.name)
      .mkString("\n") +
    "\n\n" +
    imports
      .map("extern " + _.name)
      .mkString("\n") +
    "\n\n" +
    text.emitted +
    "\n\n" +
    data.emitted +
    "\n\n" +
    rodata.emitted
  }
}

