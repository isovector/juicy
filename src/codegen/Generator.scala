package juicy.codegen

import juicy.codegen.Implicits._
import juicy.source.ast._
import juicy.source.scoper.Scope
import juicy.utils.visitor._

// NOTE: all temporaries live in ebx, except the result of malloc, and by
// association, allocators

object Generator extends GeneratorUtils {
  var currentClass: ClassDefn = null
  var currentMethod: MethodDefn = null

  val globalVtable = NamedLabel("_vtable")
  val globalArrayAlloc = NamedLabel("_aalloc")

  // array allocation
  {
    val loop = AnonLabel()
    Target.global.text.emit(
      // Array alloc assumes size will be in eax, returns alloc in eax
      // TODO: maybe make this extend from object ehhh fuckit
      globalArrayAlloc,
      Prologue(),
      "mov ecx, eax",
      "imul eax, 4",
      "add eax, 8",
      "call __malloc",
      "mov [eax+4], ecx",
      "mov edx, eax",
      "add edx, 8",
      loop,
      "mov [edx], dword 0",
      "add edx, 4",
      "sub ecx, 1",
      "cmp ecx, 0",
      s"jne $loop",
      Epilogue()
    )
  }

  Target.global.rodata.emit(globalVtable)
  Target.global.export(globalVtable)
  Target.global.export(globalArrayAlloc)

  def emit(v: Visitable): Unit = {
    v match {
      case BlockStmnt(body) =>
        body.foreach(emit)




      case ReturnStmnt(value) =>
        value.map(emit)
        Target.text.emit(Epilogue())




      case ExprStmnt(expr) =>
        emit(expr)




      case IntVal(value) => Target.text.emit(s"mov ebx, $value")

      case BoolVal(true)  => Target.text.emit(s"mov ebx, 1")
      case BoolVal(false) => Target.text.emit(s"mov ebx, 0")

      case CharVal(value) =>
        Target.text.emit(s"mov ebx, ${value.asInstanceOf[Int]}")



      case Eq(lhs, rhs)    => cmpHelper(lhs, rhs, "e")
      case GEq(lhs, rhs)   => cmpHelper(lhs, rhs, "ge")
      case LEq(lhs, rhs)   => cmpHelper(lhs, rhs, "le")
      case LThan(lhs, rhs) => cmpHelper(lhs, rhs, "l")
      case GThan(lhs, rhs) => cmpHelper(lhs, rhs, "g")

      case LazyOr(lhs, rhs)   => logical(lhs, rhs, "or", false)
      case LazyAnd(lhs, rhs)  => logical(lhs, rhs, "and", false)
      case EagerOr(lhs, rhs)  => logical(lhs, rhs, "or", true)
      case EagerAnd(lhs, rhs) => logical(lhs, rhs, "and", true)



      case Not(ghs) =>
        emit(ghs)
        Target.text.emit(
          "mov ecx, ebx",
          "mov ebx, 1",
          "sub ebx, ecx"
          )




      case c: Call =>
        // TODO: figure out how to 'this'
        // NOTE: put it in eax always i think is the plan
        val paramSize = c.args.map(_.t.stackSize).sum
        c.args.foreach { arg =>
          emit(arg)
          Target.text.emit("push ebx")
        }

        if (c.isStatic) {
          val label = c.resolvedMethod.label
          if (c.resolvedMethod.containingClass isnt currentClass)
            Target.file.reference(label)
          Target.text.emit(s"call $label")
        } else {
          Target.file.reference(globalVtable)

          val invokee = c.method.exprType.map(_.r).getOrElse(currentClass)
          val classId =
            if (invokee.isInstanceOf[ClassDefn])
              invokee.asInstanceOf[ClassDefn].classId * 4
            else
              0 // TODO: this will fail for non-classes

          val methodOffset = invokee.vmethodIndex(c.signature) * 4
          Target.text.emit(
            s"mov ecx, $globalVtable",
            s"mov ecx, [ecx+$classId]",
            s"call [ecx+$methodOffset]"
            )
        }

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
          s"; begin debugger",
          "push eax",
          "push ebx",
          "push ecx",
          "push edx",
          // Interrupt for stdout write
          "mov eax, 4",
          "mov ebx, 1",
          s"; $msg: ''$debugWhat''",
          s"mov ecx, $msg",
          s"mov edx, $msglen",
          "int 0x80",
          "pop edx",
          "pop ecx",
          "pop ebx",
          "pop eax",
          "; end debugger"
        )




      case c: ClassDefn if c.isInterface =>
        // do nothing

      case c: ClassDefn if !c.isInterface =>
        currentClass = c

        Target.file.export(c.allocLabel)
        Target.file.export(c.initLabel)
        Target.file.export(c.defaultCtorLabel)

        Target.debug.add(c)

        Target.text.emit(
          // ALLOCATOR:
          c.allocLabel,
          Prologue(),

          // Size to malloc. add 4 for the class id
          s"mov eax, ${c.allocSize + 4}",
          "call __malloc",

          s"mov [eax], dword ${c.classId}",
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
            // TODO: There is a bug here if the initializer changes eax
            "push dword [ebp-4]",
            s"call $parentInit",
            "add esp, 4"
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

        // Build vtable
        Target.file.export(c.vtableLabel)
        Target.global.reference(c.vtableLabel)

        Target.global.rodata.emit(
          s"dd ${c.vtableLabel}"
          )

        Target.rodata.emit(
          c.vtableLabel
          )

        c.allMethods.foreach { m =>
          if (!m.isCxr) {
            if (m.containingClass isnt c)
              Target.file.reference(m.label)

            Target.rodata.emit(
              s"dd ${m.label}"
            )
          }
        }


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
              val ctor = parent.rc.defaultCtorLabel
              Target.file.reference(ctor)
              Target.text.emit(s"call $ctor")
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
        val thenL = AnonLabel("then")
        val afterL = AnonLabel("after")
        val elseL =
          if (i.otherwise.isDefined)
            AnonLabel("else")
          else
            afterL

        // Condition, jump to else
        emit(i.cond)
        Target.text.emit(
          "test ebx, 1",
          s"jz $elseL",
          thenL // not strictly necessary, but helpful for debugging
        )

        emit(i.then)
        if (i.otherwise.isDefined) {
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
          case sm: StaticMember =>
            Target.data.emit("; unimplemented static assignment")

          case idx: Index =>
            // TODO
            Target.data.emit("; unimplemented index assignment")
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
        val c = n.tname.rc
        val alloc = c.allocLabel
        val ctor = n.resolvedCxr.label

        if (c isnt currentClass) {
          Target.file.reference(alloc)
          Target.file.reference(ctor)
        }

        Target.text.emit(
          s"call $alloc",
          "push eax",
          s"call $ctor",
          "add esp, 4",
          // allocator returns this in eax
          "mov ebx, eax"
          )


      case na@NewArray(t: Typename, len: Expression) =>
        Target.file.reference(globalArrayAlloc)

        emit(len)
        Target.text.emit(
          "mov eax, ebx",
          s"call $globalArrayAlloc",
          //s"mov [eax], ${t.r.classId}",
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
