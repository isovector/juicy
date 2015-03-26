import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

import juicy.codegen._
import juicy.codegen.Implicits._
import juicy.source.ast._
import juicy.source.parser._
import juicy.source.tokenizer._

class CodegenSpec extends FlatSpec with ShouldMatchers {
  def mkParser(source: String) = new Parser(new TokenStream(source))

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

  it should "emit elses" in {
    Target.withFile("debug") {
      mkParser("""
        if (true) {
          if (false)
            x;
          } else {
            y;
          }
        """).parseStmnt().emit

      println(Target.file.emitted)
    }
  }

  it should "emit debuggers" in {
    Target.withFile("debug") {
      val debugger = mkParser("""#YOLO "wagwan homie";""").parseStmnt()
      debugger should be === Debugger("wagwan homie")
      debugger.emit
      println(Target.file.emitted)
    }
  }
}
