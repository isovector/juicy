package juicy.codegen

import juicy.codegen.Implicits._
import juicy.source.ast._
import juicy.source.scoper.Scope
import juicy.utils.visitor._

// NOTE: all temporaries live in ebx, except the result of malloc, and by
// association, allocators

object Generator {
  var currentClass: ClassDefn = null
  var currentMethod: MethodDefn = null

  case class Location(reg: String, offset: Int) {
    lazy val deref = s"[$reg+$offset]"
  }

  // Get the memory location of a variable
  def varLocation(name: String, scope: Scope) = {
    // TODO: this fails for non-static fields
    if (currentMethod != null) {
      val params = currentMethod.params.length

      // `this` is always the first thing pushed
      val stackOffset =
        if (name == "this")
          0
        else
          scope.localVarStackIndex(name)

      // Check if our offset number is < num of params, if so, we are up the
      // stack, otherwise down
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

  // Location of a reference's member
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
        // NOTE: put it in eax always i think is the plan
        val paramSize = c.args.map(_.t.stackSize).sum
        c.args.foreach { arg =>
          emit(arg)
          Target.text.emit("push ebx")
        }

        // TODO: this should check if it's static, and if so do this
        // otherwise dynamic dispatch
        val label = c.resolvedMethod.label
        Target.text.emit(s"call $label")

        // Revert stack to old position after call
        if (paramSize > 0)
          Target.text.emit(s"add esp, byte $paramSize")




      case Debugger(debugWhat) =>
        val msg = GlobalAnonLabel("debugstr")
        val msglen = GlobalAnonLabel("debugstrln")
        Target.fromGlobal(msg, msglen)

        // Generate string and strlen
        Target.global.data.emit((msg, "db \"" + debugWhat + "\", 10"))
        Target.global.data.emit((msglen, s"equ $$ - $msg"))

        Target.text.emit(
          "push eax",
          "push ebx",
          "push ecx",
          "push edx",
          // Interrupt for stdout write
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

          // Size to malloc. add 4 for the class id
          s"mov eax, ${c.allocSize + 4}",
          "call __malloc",

          s"mov [eax], word ${c.classId}",
          "push eax", // TODO: uhh so how do we do this passing?
          s"call ${c.initLabel}",

          // Allocator should return `this`, so put it into ebx
          "pop ebx",
          Epilogue(),

          // INITIALIZER
          c.initLabel,
          Prologue()
        )

        // Call parent initializer if it exists (it does, except for Object)
        if (c.extnds.length > 0) {
          val parentInit = c.extnds(0).r.asInstanceOf[ClassDefn].initLabel
          Target.file.reference(parentInit)

          Target.text.emit(
            "push word [ebp+4]"//,
            // TODO: when we link back to stdlib, do this properly
            //s"call $parentInit"
          )
        }

        // Initialize each field
        c.fields.foreach { f =>
          emit(f)
        }

        Target.text.emit(
          Epilogue()
          )

        // Emit methods
        c.methods.foreach(emit)
        currentClass = null




      case m: MethodDefn =>
        currentMethod = m

        // Are we `public static int test()`? If so, generate a _start symbol
        if (m.isEntry) {
          val startLabel = NamedLabel("start")
          Target.file.export(startLabel)
          Target.text.emit(
            startLabel,
            // TODO: refactor this out if we need to generate vtables here

            // Call test()
            s"call ${m.label}",

            // unix return interrupt
            "mov eax, 1",
            "int 0x80"
          )
        }

        Target.file.export(m.label)
        Target.text.emit(
          m.label,
          Prologue()
        )

        // Reserve local stack space if necessary
        val stackSize =
          (m.scope.get.maxStackIndex - m.params.length) * 4
        if (stackSize > 0)
          Target.text.emit(s"sub esp, $stackSize")

        if (m.isCxr) {
          m
            .containingClass
            .extnds
            .headOption
            .map { parent =>
              // TODO: when we have stdlib, uncomment this
              // Target.text.emit(s"call ${parent.rc.defaultCtorLabel}")
            }
        }

        // Generate method body
        m.body.map(emit)
        Target.text.emit(Epilogue())

        currentMethod = null




      case i: Id =>
        val offset = varLocation(i.name, i.scope.get).deref
        Target.text.emit(
          s"; load var ${i.name}",
          s"mov ebx, $offset")



      case t: ThisVal =>
        val offset = varLocation("this", t.scope.get).deref
        Target.text.emit(
          s"; load this",
          s"mov ebx, $offset")




      case i: IfStmnt =>
        val elseL = AnonLabel("else")

        // Condition, jump to else
        emit(i.cond)
        Target.text.emit(RawInstr(s"jne $elseL"))

        emit(i.then)
        if (i.otherwise.isDefined) {
          // We reuse our labels for posterity. or something
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
        // Outermost expr of LHS must not be dereferenced, so we can't just
        // delegate to the other emissions in this file to do it for us
        a.lhs.expr match {
          case i: Id =>
            val offset = varLocation(i.name, i.scope.get)
            // Must be two instructions because otherwise nasm will dereference
            Target.text.emit(
              s"mov ebx, ${offset.reg}",
              s"add ebx, ${offset.offset}")

          case m: Member =>
            // Emit the lhs of the method because only OUTERMOST expr must not
            // be deref'd
            emit(m.lhs)
            val offset = memLocation(m)
            Target.text.emit(
              s"add ebx, ${offset.offset}")

          // TODO: do static members (probably stupid easy, but we don't gen
          // them yet)
          case sm: StaticMember => throw new Exception("static dont work sucka")
        }

        // lvalue is now in ebx, but it will get stomped by rhs, so push it
        Target.text.emit("push ebx")
        // rhs is in ebx so we can chain assignments for free
        emit(a.rhs)
        Target.text.emit(
          "pop ecx",
          // *ecx = ebx
          "mov [ecx], ebx"
        )


      case n: NewType =>
        // TODO: do we need to store anything?
        val alloc = n.tname.rc.allocLabel
        val ctor = n.resolvedCxr.label
        Target.text.emit(
          s"call $alloc",
          "push eax",
          s"call $ctor",
          "add esp, 4",
          // allocator returns this in eax
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
