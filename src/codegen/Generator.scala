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
  val hierarchyTable = NamedLabel("_hierarchy")
  val globalArrayAlloc = NamedLabel("_aalloc")
  val gStaticInit = NamedLabel("_static")

  // array allocation
  {
    val loop = AnonLabel()
    val after = AnonLabel()
    Target.global.text.emit(
      // Array alloc assumes size will be in eax, returns alloc in eax
      globalArrayAlloc,
      Prologue(),
      "mov ecx, eax",
      "imul eax, 4",
      "add eax, 8",
      "call __malloc",
      "mov [eax+4], ecx",
      "cmp ecx, 0",
      s"je $after",
      "mov edx, eax",
      "add edx, 8",
      loop,
      "mov [edx], dword 0",
      "add edx, 4",
      "sub ecx, 1",
      "cmp ecx, 0",
      s"jne $loop",
      after,
      Epilogue()
    )
  }

  // instance of
  {
    // NOTE: pass var's runtime type in eax, desired runtime type in ebx
    val loop = AnonLabel()
    val noret = AnonLabel("different")
    Target.global.text.emit(
      gInstanceOf,
      Prologue(),
      "mov ecx, __hierarchy",
      "mov edx, eax",
      "imul edx, 4",
      "mov ecx, [ecx+edx]",
      loop,
      "cmp ebx, [ecx]",
      s"jne $noret",
      "mov ebx, dword 1",
      Epilogue(),
      noret,
      "add ecx, 4",
      "cmp [ecx], dword -1",
      s"jne $loop",
      "mov ebx, dword 0",
      Epilogue()
    )
  }

  Target.global.export(globalVtable)
  Target.global.export(globalArrayAlloc)
  Target.global.export(gInstanceOf)



  def emit(v: Visitable): Unit = {
    v match {
      case BlockStmnt(body) =>
        body.foreach(emit)

      case ReturnStmnt(value) =>
        value.map(emit)
        Target.text.emit(Epilogue())

      case ExprStmnt(expr) =>
        emit(expr)




      case NullVal() => Target.text.emit(s"mov ebx, dword 0")

      case IntVal(value) => Target.text.emit(s"mov ebx, $value")

      case BoolVal(true)  => Target.text.emit(s"mov ebx, 1")
      case BoolVal(false) => Target.text.emit(s"mov ebx, 0")

      case CharVal(value) =>
        Target.text.emit(s"mov ebx, ${value.asInstanceOf[Int]}")

      case s: StringVal =>
        Target.file.reference(s.interned)
        Target.text.emit(s"mov ebx, ${s.interned}")

      case Eq(lhs, rhs)    => cmpHelper(lhs, rhs, "e")
      case NEq(lhs, rhs)   => cmpHelper(lhs, rhs, "ne")
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


      case Neg(ghs) =>
        emit(ghs)
        Target.text.emit(
          "neg ebx"
        )

      case c: Call if c.isStatic =>
        val paramSize = c.args.map(_.t.stackSize).sum
        c.args.foreach { arg =>
          emit(arg)
          Target.text.emit("push ebx")
        }

        val label = c.resolvedMethod.label
        if (c.resolvedMethod.containingClass isnt currentClass)
            Target.file.reference(label)
        Target.text.emit(s"call $label")

        // Revert stack to old position after call
        if (paramSize > 0)
          Target.text.emit(s"add esp, byte $paramSize")



      case c: Call if !c.isStatic =>
        Target.file.reference(globalVtable)
        val invokee = c.resolvedMethod.containingClass

        c.method.expr match {
          case i: Id => emit(ThisVal())
          case m@Member(lhs, rhs) =>
            emit(lhs)
        }

        Target.text.emit(
            Guard(
              "cmp ebx, 0", "jne",
              "ref_ok"),
            s"push ebx")

        val paramSize = c.args.map(_.t.stackSize).sum
        c.args.foreach { arg =>
          emit(arg)
          Target.text.emit("push ebx")
        }

        val methodOffset = invokee.vmethodIndex(c.signature) * 4
        val tableLabel =
          if (invokee.isInterface)
            invokee.itableLabel
          else
            globalVtable

        Target.file.reference(tableLabel)
        Target.text.emit(
          s"; ${invokee.name} => ${methodOffset}",
          s"; call ${c.signature}",
          s"mov ecx, $tableLabel",
          s"mov edx, [esp+${paramSize}]",
          s"mov edx, [edx]",
          s"mov ecx, [ecx + edx * 4]",
          s"; ${c.signature}",
          s"call [ecx+$methodOffset]",
          s"add esp, byte ${paramSize + 4}"
        )




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

        // Build the class hierarchy
        Target.file.export(c.hierarchyLabel)
        Target.rodata.emit(c.hierarchyLabel)

        Target.rodata.emit(s"; hierarchy for ${c.name}")
        (c +: c.superTypes).distinct.map { t =>
          Target.rodata.emit(s"dd ${Runtime.lookup(t)}; id for ${t.name}")
        }
        Target.rodata.emit(s"dd -1; end of hierarchy")

        // Build the array's class hierarchy
        Target.file.export(c.arrayOf.hierarchyLabel)
        Target.rodata.emit(c.arrayOf.hierarchyLabel)
        Target.rodata.emit(s"; hierarchy for arrays of ${c.name}")
        ((c.arrayOf +: c.arrayOf.superTypes) ++ c.superTypes.map(_.arrayOf)).distinct.map { t =>
          Target.rodata.emit(s"dd ${Runtime.lookup(t)}; id for ${t.name}")
        }
        Target.rodata.emit(s"dd -1; end of hierarchy")

        Target.file.export(c.allocLabel)
        Target.file.export(c.initLabel)
        Target.file.export(c.defaultCtorLabel)
        Target.file.export(c.vtableLabel)
        Target.file.export(c.staticInitLabel)

        c.allInterfaces.foreach { int =>
          Target.file.export(c itableFor int)
          Target.rodata.emit(c itableFor int)

          int.allMethods.map(_.signature).foreach { sig =>
            val meth = c.allMethods.find(_.signature == sig).get
            if (c isnt meth.containingClass) {
              Target.file.reference(meth.label)
            }

            Target.rodata.emit(s"dd ${meth.label}")
          }
        }

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
            "push eax",
            "push dword [ebp-4]",
            s"call $parentInit",
            "add esp, 4",
            "pop eax"
          )
        }

        // Initialize each field
        c
          .fields
          .filter(!_.isStatic)
          .foreach { f =>
            Target.text.emit("push eax")
            emit(f)
            Target.text.emit("pop eax")
          }

        Target.text.emit(
          Epilogue(),

          // Static initializer
          c.staticInitLabel,
          Prologue()
          )

        c
          .fields
          .filter(_.isStatic)
          .foreach { f =>
            Target.file.export(f.staticLabel)
            Target.data.emit(
              (f.staticLabel, s"dw 0")
            )
            emit(f)
          }

        Target.text.emit(Epilogue())

        // Emit methods
        c.methods.foreach(emit)

        Target.rodata.emit(c.vtableLabel)
        c
          .vMethods
          .foreach { m =>
          if (!m.isCxr) {

            if (m.containingClass isnt c)
              Target.file.reference(m.label)

            Target.rodata.emit(
              s"dd ${m.label}"
            )
          }
        }


        currentClass = null




      case m: MethodDefn if !m.isNative =>
        currentMethod = m

        // Are we `public static int test()`? If so, generate a _start symbol
        if (m.isEntry) {
          val startLabel = NamedLabel("start")
          Target.file.reference(gStaticInit)
          Target.file.export(startLabel)
          Target.text.emit(
            startLabel,

            s"call ${gStaticInit}",
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

        // Reserve local stack space if necess ary
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

              emit(ThisVal())
              Target.text.emit(
                "push ebx",
                s"call $ctor",
                "add esp, 4"
              )
            }
        }

        // Generate method body
        m.body.map(emit)
        Target.text.emit(Epilogue())

        currentMethod = null


      case m: MethodDefn if m.isNative =>
        val label = ExplicitLabel(s"NATIVE${m.canonicalName}")

        Target.file.reference(label)
        Target.file.export(m.label)
        Target.text.emit(
          m.label,
          Prologue(),
          "mov eax, [ebp+8]",
          s"call $label",
          "mov ebx, eax",
          Epilogue()
          )




      case i: Id =>
        val offset = varLocation(i.name, i.scope.get).deref
        Target.text.emit(
          s"; load var ${i.name}",
          s"mov ebx, $offset")



      case t: ThisVal =>
        val loc = thisLocation.deref
        Target.text.emit(
          s"; load this",
          s"mov ebx, $loc"
        )



      case w: WhileStmnt =>
        val loop = AnonLabel("while_body")
        val after = AnonLabel("while_end")
        Target.text.emit(loop)
        emit(w.cond)
        Target.text.emit(
          "cmp ebx, 1",
          s"jne $after"
          )
        emit(w.body)
        Target.text.emit(
          s"jmp $loop",
          after)



      case f: ForStmnt =>
        val loop = AnonLabel("for_body")
        val after = AnonLabel("for_end")

        f.first.map(emit)
        Target.text.emit(loop)

        f.cond match {
          case Some(cond) => emit(cond)
          case None =>
            Target.text.emit("mov ebx, dword 1")
        }

        Target.text.emit(
          "cmp ebx, 1",
          s"jne $after"
          )
        emit(f.body)

        f.after.map(emit)
        Target.text.emit(
          s"jmp $loop",
          after)



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
          val offset =
            if (v.isStatic)
              staticLocation(v)
            else
              varLocation(v.name, v.scope.get)

          Target.text.emit(s"; init var ${v.name}")
          v.value.map(emit)
          if (offset.isMagic) {
            Target.text.emit(
              "push ecx",
              s"mov ecx, ${offset.deref}",
              "mov [ecx], ebx",
              "pop ecx"
            )
          } else {
            if (currentMethod == null) {
              Target.text.emit("push ebx")
              emit(ThisVal())
              Target.text.emit("mov eax, ebx")
              Target.text.emit("pop ebx")
            }
            Target.text.emit(s"mov ${offset.deref}, ebx")
          }
        }


      case a: Assignment =>
        // Outermost expr of LHS must not be dereferenced, so we can't just
        // delegate to the other emissions in this file to do it for us
        var isIndex = false
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

          case s: StaticMember =>
            staticHelper(s.decl.get)

          case idx: Index =>
            isIndex = true
            idxHelper(idx, !a.rhs.et.isPrimitive)
            Target.text.emit(
              "imul ecx, 4",
              "add ebx, ecx",
              "add ebx, 8"
            )

        }

        // lvalue is now in ebx, but it will get stomped by rhs, so push it
        Target.text.emit("push ebx")
        // rhs is in ebx so we can chain assignments for free
        emit(a.rhs)
        Target.text.emit(
          "pop ecx",
          "; *ecx = ebx",
          "mov [ecx], ebx"
        )

        if (isIndex && !a.rhs.et.isPrimitive) {
            val label = AnonLabel("not_null")
            val after = AnonLabel()

            Target.file.reference(gInstanceOf)
            Target.text.emit(
              "; array store exceptions",
              "cmp ebx, 0",
              s"jne $label",
              "mov ebx, 0",
              s"jmp $after",
              label,
              "mov eax, [ebx]",
              "pop ecx",
              "push ebx",
              "mov ebx, ecx",
              "sub ebx, 1",
              s"call $gInstanceOf",
              Guard(
                "cmp ebx, 0", "jne",
                "store_ok"
                ),
              "pop ebx",
              after
            )
        }


      case n: NewType =>
        val c = n.tname.rc
        val alloc = c.allocLabel
        val ctor = n.resolvedCxr.label

        if (c isnt currentClass) {
          Target.file.reference(alloc)
          Target.file.reference(ctor)
        }

        Target.text.emit(
          s"call $alloc",
          "push eax"
        )

        n.args.foreach { arg =>
          emit(arg)
          Target.text.emit("push ebx")
        }

        Target.text.emit(
          s"call $ctor",
          s"add esp, ${n.args.length * 4}",
          // `this` is still on the stack
          "pop ebx"
          )


      case na@NewArray(t: Typename, len: Expression) =>
        Target.file.reference(globalArrayAlloc)

        emit(len)
        Target.text.emit(
          "mov eax, ebx",
          s"call $globalArrayAlloc",
          s"mov [eax], dword ${Runtime.lookup(t.r)}",
          "mov ebx, eax"
          )



      case m: Member =>
        emit(m.lhs)
        Target.text.emit(s"mov ebx, ${memLocation(m).deref}")


      case idx: Index =>
        idxHelper(idx, false)
        Target.text.emit("mov ebx, [ebx+ecx*4+8]")





      case io: InstanceOf =>
        emit(io.lhs)
        instanceOfHelper(io.tname.r)


      case c: Cast =>
        if (c.tname.r.isPrimitive && c.value.et.isPrimitive) {
          val from = c.value.t.asInstanceOf[PrimitiveDefn]
          val to = c.tname.r.asInstanceOf[PrimitiveDefn]
          emit(c.value)
          if (PrimitiveDefn.ShouldClamp(from, to)) {
            val mask = to.clampMask
            Target.text.emit(
              s"; clamping for ${from.name} => ${to.name}",
              s"and ebx, dword $mask"
            )
          }
          if (PrimitiveDefn.ShouldExtend(from, to)) {
            val extend = (from.clampMask ^ to.clampMask)
            val nshift = from.numBytes * 8 - 1
            val dontshift = AnonLabel("dontShift")
            val shifted = AnonLabel("shifted")
            Target.text.emit(
              s";sign extension for ${from.name} => ${to.name}",
              s"push ebx",
              s";determine MSB of current",
              s"shr ebx, $nshift",
              s"test ebx, 1",
              s"jz $dontshift",
              s"pop ebx",
              s"or ebx, $extend",
              s"jmp $shifted",
              dontshift,
              s"pop ebx",
              shifted
            )
          }
        } else {
          val after = AnonLabel("null_cast");

          emit(c.value)
          Target.text.emit(
            "cmp ebx, 0",
            s"je $after",
            "push ebx"
          )

          instanceOfHelper(c.tname.r)
          Target.text.emit(
            Guard(
              "cmp ebx, 0", "jne",
              "good_cast"),
            "pop ebx",
            after
          )

        }


      case s: StaticMember =>
        staticHelper(s.decl.get)
        Target.text.emit("mov ebx, [ebx]")




      case op: Add => arithmetic(op, "add")
      case op: Sub => arithmetic(op, "sub")
      case op: Mul => arithmetic(op, "imul")
      case op: Div =>
        arithmetic(op, "idiv")
        Target.text.emit("mov ebx, eax")
      case op: Mod =>
        arithmetic(op, "idiv")
        Target.text.emit("mov ebx, edx")

      case concat: StringConcat =>
        emit(concat.lhs)
        Target.text.emit("push ebx")
        emit(concat.rhs)
        if (Runtime.lookup(currentClass) != Runtime.string) {
          Target.file.reference(Runtime.stringConcat)
        }
        Target.text.emit(
          s"push ebx",
          s"call ${Runtime.stringConcat}",
          s"add esp, 8"
        )

      case toS: BoolToStr  => toStringHelper(toS, Runtime.boolToString)
      case toS: ByteToStr  => toStringHelper(toS, Runtime.byteToString)
      case toS: CharToStr  => toStringHelper(toS, Runtime.charToString)
      case toS: IntToStr   => toStringHelper(toS, Runtime.intToString)
      case toS: ShortToStr => toStringHelper(toS, Runtime.shortToString)
      case toS: RefToStr   => toStringHelper(toS, Runtime.objToString)

      case otherwise =>
        Target.text.emit(
          s"; not implemented: ${otherwise.toString.takeWhile(_ != '(')}")

    }
  }
}
