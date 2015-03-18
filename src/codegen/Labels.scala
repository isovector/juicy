package juicy.codegen

trait Label extends Instruction {
  val name: String
  def emitted = name + ":"

  override def toString = name
}

object AnonLabel {
  protected var nextLabel = 0

  def getLabel: String = {
    nextLabel += 1
    s"__anon_$nextLabel"
  }


  def getLabel(semantic: String): String = {
    getLabel + s"_$semantic"
  }
}

case class AnonLabel(semantic: String = "") extends Label {
  val name =
    if (semantic.isEmpty)
      AnonLabel.getLabel
    else
      AnonLabel.getLabel(semantic)
}

case class NamedLabel(name: String) extends Label

