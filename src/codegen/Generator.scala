package juicy.codegen

import juicy.codegen.Implicits._
import juicy.source.ast._
import juicy.utils.visitor._

object Generator {
  var currentMethod: MethodDefn = null

  def emit(v: Visitable): Unit = {
    v match {
      case BlockStmnt(body) =>
        body.foreach(emit)




      case ReturnStmnt(value) =>
        value.map(emit)
        Target.text.emit(Epilogue())




      case ExprStmnt(expr) =>
        emit(expr)




      case IntVal(value) =>
        Target.text.emit(s"mov ebx,$value")




      case c: Call =>
        // TODO: figure out how to 'this'
        val paramSize = c.args.map(_.t.stackSize).sum
        c.args.foreach { arg =>
          emit(arg)
          Target.text.emit("push ebx")
        }

        val label = c.resolvedMethod.label
        Target.text.emit(s"call $label")

        if (paramSize > 0)
          Target.text.emit(s"add esp, byte $paramSize")




      case Debugger(debugWhat) =>
        val msg = GlobalAnonLabel("debugstr")
        val msglen = GlobalAnonLabel("debugstrln")
        Target.fromGlobal(msg, msglen)

        Target.global.data.emit((msg, "db \"" + debugWhat + "\", 10"))
        Target.global.data.emit((msglen, s"equ $$ - $msg"))

        Target.text.emit(
          "mov eax, 4",
          "mov ebx, 1",
          s"mov ecx, $msg",
          s"mov edx, $msglen",
          "int 0x80"
        )




      case c: ClassDefn =>
        Target.file.export(c.allocLabel)
        Target.file.export(c.initLabel)
        Target.file.export(c.defaultCtorLabel)
        Target.text.emit(
          // ALLOCATOR:
          c.allocLabel,
          Prologue(),
          s"mov eax, ${c.allocSize}",
          //"call __malloc",
          s"mov eax, ${c.classId}",
          "push eax", // TODO: uhh so how do we do this passing?
          s"call ${c.initLabel}",
          Epilogue(),

          // INITIALIZER
          c.initLabel,
          Prologue()
        )

        if (c.extnds.length > 0) {
          val parentInit = c.extnds(0).r.asInstanceOf[ClassDefn].initLabel
          Target.file.reference(parentInit)
          // TODO: when we link back to stdlib, do this properly
          Target.text.emit(
            //"pushw [ebp-4]", // TODO: is this THIS?
            //s"call $parentInit"
          )
        }

        Target.text.emit(
          Epilogue()
          )



        c.methods.foreach(emit)




      case m: MethodDefn =>
        currentMethod = m

        if (m.isEntry) {
          val startLabel = NamedLabel("start")
          Target.file.export(startLabel)
          Target.text.emit(
            startLabel,
            s"call ${m.label}",
            "mov eax, 1",
            "int 0x80"
          )
        }

        Target.file.export(m.label)
        Target.text.emit(
          m.label,
          Prologue()
          // TODO: allocate stack space
        )
        m.body.map(emit)
        Target.text.emit(Epilogue())




      case i: Id =>
        // relative to local scope
        val stackOffset = i.stackOffset
        val params = currentMethod.params.length
        val offset =
          if (stackOffset < params)
            params - stackOffset + 1
          else
            -(stackOffset - params)

        Target.text.emit(s"mov ebx, [ebp+${4 * offset}]")




      case i: IfStmnt =>
        val elseL = AnonLabel("else")

        emit(i.cond)
        Target.text.emit(RawInstr(s"jne $elseL"))

        emit(i.then)
        if (i.otherwise.isDefined) {
          val afterL = AnonLabel("after")
          Target.text.emit(
            RawInstr(s"jmp $afterL"),
            elseL)
          emit(i.otherwise.get)

          Target.text.emit(afterL)
        } else Target.text.emit(elseL)




      case otherwise =>
        Target.text.emit(
          s"; not implemented: ${otherwise.toString.takeWhile(_ != '(')}")

    }
  }
}
