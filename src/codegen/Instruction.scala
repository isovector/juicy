package juicy.codegen

trait Emittable {
  def emitted: String
}

trait Instruction extends Emittable

case class RawInstr(raw: String) extends Instruction {
  def emitted = raw
}

case class DataDefnInstr(l: Label, ins: Instruction) extends Instruction {
  def emitted = s"${l.emitted} ${ins.emitted}"
}

case class SysExitInstr(code: Int) extends Instruction {
  def emitted =
    Seq(
      "mov eax, 1",
      s"mov ebx, $code",
      "int 0x80"
    ).mkString("\n")
}

case class Prologue() extends Instruction {
  def emitted =
    Seq(
      "push ebp",
      "mov ebp, esp"
    ).mkString("\n")
}

case class Epilogue() extends Instruction {
  def emitted =
    Seq(
      "leave",
      "ret"
    ).mkString("\n")
}

