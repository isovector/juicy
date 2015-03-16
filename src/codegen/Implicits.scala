package juicy.codegen

package object Implicits {
  implicit def toRawInstr(u: String): RawInstr = RawInstr(u)
  implicit def toDataDefnInstrIns(u: (Label, Instruction)): DataDefnInstr =
    DataDefnInstr(u._1, u._2)
  implicit def toDataDefnInstrStr(u: (Label, String)): DataDefnInstr =
    DataDefnInstr(u._1, toRawInstr(u._2))
}

