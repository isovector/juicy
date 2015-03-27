package juicy.codegen

import juicy.codegen.Implicits._
import juicy.source.ast._
import juicy.source.scoper.Scope
import juicy.utils.visitor._

object Generator {
  var currentClass: ClassDefn = null
  var currentMethod: MethodDefn = null

  case class Location(reg: String, offset: Int) {
    lazy val deref = s"[$reg+$offset]"
  }

  def varLocation(name: String, scope: Scope) = {
    if (currentMethod != null) {
      val params = currentMethod.params.length
      val stackOffset = scope.localVarStackIndex(name)

      val offset =
        if (stackOffset < params)
          params - stackOffset + 1
        else
          -(stackOffset - params + 1)

      Location("ebp", 4 * offset)
    } else {
      val offset = currentClass.getFieldIndex(name)
      Location("eax", 4 * offset)
    }
  }

  def memLocation(m: Member) = {
    val t = m.lhs.t
    if (!t.isInstanceOf[ClassDefn]) throw new Exception("array access?")

    val c = t.asInstanceOf[ClassDefn]
    val offset = c.getFieldIndex(m.rhs.name)

    // TODO: broken if not in ebx, but should always be
    Location("ebx", offset * 4)
  }

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
        Target.text.emit(s"mov ebx, $value")




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
          "push eax",
          "push ebx",
          "push ecx",
          "push edx",
          "mov eax, 4",
          "mov ebx, 1",
          s"mov ecx, $msg",
          s"mov edx, $msglen",
          "int 0x80",
          "pop edx",
          "pop ecx",
          "pop ebx",
          "pop eax"
        )




      case c: ClassDefn =>
        currentClass = c

        Target.file.export(c.allocLabel)
        Target.file.export(c.initLabel)
        Target.file.export(c.defaultCtorLabel)

        Target.text.emit(
          // ALLOCATOR:
          c.allocLabel,
          Prologue(),
          s"mov eax, ${c.allocSize}",
          "call __malloc",
          s"mov [eax], word ${c.classId}",
          "push eax", // TODO: uhh so how do we do this passing?
          s"call ${c.initLabel}",
          "pop ebx",  // put THIS into return addr
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
            "push word [ebp+4]"//,
            //s"call $parentInit"
          )
        }

        c.fields.foreach { f =>
          emit(f)
        }

        Target.text.emit(
          Epilogue()
          )



        c.methods.foreach(emit)
        currentClass = null




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

        val stackSize =
          (m.scope.get.maxStackIndex - m.params.length) * 4

        Target.file.export(m.label)
        Target.text.emit(
          m.label,
          Prologue()
        )

        if (stackSize > 0)
          Target.text.emit(s"sub esp, $stackSize")

        m.body.map(emit)
        Target.text.emit(Epilogue())

        currentMethod = null




      case i: Id =>
        // relative to local scope
        val offset = varLocation(i.name, i.scope.get).deref
        Target.text.emit(
          s"; load var ${i.name}",
          s"mov ebx, $offset")




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




      case v: VarStmnt =>
        if (v.value.isDefined) {
          val offset = varLocation(v.name, v.scope.get).deref

          Target.text.emit(s"; init var ${v.name}")
          v.value.map(emit)
          Target.text.emit(s"mov $offset, ebx")
        }


      case a: Assignment =>
        a.lhs.expr match {
          case i: Id =>
            val offset = varLocation(i.name, i.scope.get)
            Target.text.emit(
              s"mov ebx, ${offset.reg}",
              s"add ebx, ${offset.offset}")

          case m: Member =>
            emit(m.lhs)
            val offset = memLocation(m)
            Target.text.emit(
              s"add ebx, ${offset.offset}")

          case sm: StaticMember => throw new Exception("static dont work sucka")
        }

        Target.text.emit("push ebx")
        emit(a.rhs)
        Target.text.emit(
          "pop ecx",
          "mov [ecx], ebx"
        )


      case n: NewType =>
        // TODO: do we need to store anything?
        val init = n.tname.rc.allocLabel
        Target.text.emit(
          s"call $init",
          // TODO: call constructor
          "mov ebx, eax"
          )


      case m: Member =>
        emit(m.lhs)
        Target.text.emit(s"mov ebx, ${memLocation(m).deref}")



      case otherwise =>
        Target.text.emit(
          s"; not implemented: ${otherwise.toString.takeWhile(_ != '(')}")

    }
  }
}
