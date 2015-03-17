package juicy.codegen

trait Label extends Instruction {
  val name: String
  def emitted = name + ":"

  override def toString = name
}

object AnonLabel {
  protected var nextLabel = 0

  def getLabel = {
    nextLabel += 1
    "__anon_" + nextLabel.toString
  }
}

case class AnonLabel() extends Label {
  val name = AnonLabel.getLabel
}

case class NamedLabel(name: String) extends Label

