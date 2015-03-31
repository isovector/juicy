package juicy.codegen

trait Label extends Instruction {
  val name: String
  def emitted = name + ":"

  override def toString = name

  override def equals(o: Any) = {
    o match {
      case l: Label => l.name == name
      case _ => false
    }
  }

  override def hashCode = name.hashCode
}

object AnonLabel {
  protected var nextLabel = 0

  def getLabel: String = {
    nextLabel += 1
    s"__anon_$nextLabel"
  }


  def getLabel(semantic: String): String = {
    getLabel + s".$semantic"
  }
}

case class GlobalAnonLabel(semantic: String = "") extends Label {
  val name =
    if (semantic.isEmpty)
      AnonLabel.getLabel
    else
      AnonLabel.getLabel(semantic)
}

case class AnonLabel(semantic: String = "") extends Label {
  val name =
    if (semantic.isEmpty)
      "." + AnonLabel.getLabel
    else
      "." + AnonLabel.getLabel(semantic)
}

case class NamedLabel(earlyName: String) extends Label {
  val name = s"_$earlyName"
}

case class ExplicitLabel(name: String) extends Label

