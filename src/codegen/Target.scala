package juicy.codegen

class Section(which: String) extends Emittable {
  private val instructions = collection.mutable.MutableList[Instruction]()

  private def emitImpl(ins: Instruction) = {
    instructions += ins
  }

  def emit(ins: Instruction*): Unit = {
    ins.toList.foreach(emitImpl)
  }

  emit(RawInstr(s"section .$which"))
  def emitted = {
    instructions.map(_.emitted).mkString("\n")
  }
}

class Target extends Emittable {
  private val imports = collection.mutable.Set[Label]()
  private val exports = collection.mutable.Set[Label]()

  def export(l: Label) = {
    exports += l
  }

  def reference(l: Label) = {
    imports += l
  }

  val text = new Section("text")
  val data = new Section("data")

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
    data.emitted
  }
}

