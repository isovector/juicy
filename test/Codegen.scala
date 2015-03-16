import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

import juicy.codegen._
import juicy.codegen.Implicits._

class CodegenSpec extends FlatSpec with ShouldMatchers {
  "Codegen" should "write a useful hello world" in {
    val t = new Target

    def func(name: String) = {
      val l = NamedLabel(name)
      t.export(l)
      t.text.emit(l)
      l
    }

    func("_start")
    val msg = NamedLabel("msg")
    t.data.emit((msg, "db \"Hello, world!\", 10"))

    val msglen = NamedLabel("msglen")
    t.data.emit((msglen, s"equ $$ - $msg"))

    t.text.emit(
      "mov eax, 4",
      "mov ebx, 1",
      s"mov ecx, $msg",
      s"mov edx, $msglen",
      "int 0x80",
      SysExitInstr(99)
    )

    println(t.emitted)
  }
}
