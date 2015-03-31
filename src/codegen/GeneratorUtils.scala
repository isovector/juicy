package juicy.codegen

import juicy.codegen.Implicits._
import juicy.source.ast._
import juicy.source.scoper.Scope
import juicy.utils.visitor._

object Runtime {
  var int = -1
  var obj = -1
  var bool = -1
  var byte = -1
  var char = -1
  var short = -1
  var intBox = -1
  var string = -1
  var boolBox = -1
  var byteBox = -1
  var charBox = -1
  var shortBox = -1
  def charArray = char + 1

  def numericBoxes = Seq(intBox, byteBox, charBox, shortBox)

  def setClass(c: ClassDefn): Unit = {
    val id = c.classId
    tLookup += c.labelName -> id
    if (c.pkg != Seq("java", "lang"))
      return

    c.name match {
      case "Object"    => obj = id
      case "Boolean"   => boolBox = id
      case "Byte"      => byteBox = id
      case "Character" => charBox = id
      case "Integer"   => intBox = id
      case "Short"     => shortBox = id
      case "String"    => string = id
      case _           =>
    }
  }

  def setPrimitive(c: PrimitiveDefn): Unit = {
    val id = c.classId
    tLookup += c.labelName -> id

    c.name match {
      case "int"     => int = id
      case "byte"    => byte = id
      case "char"    => char = id
      case "short"   => short = id
      case "boolean" => bool = id
      case _         =>
    }
  }

  private val tLookup = collection.mutable.Map[String, Int]()
  def lookup(t: TypeDefn): Int = {
    val ln = t.labelName
    val isArray = t.isInstanceOf[ArrayDefn]

    if (isArray)
      tLookup(t.labelName.init) + 1
    else
      tLookup(t.labelName)
  }
}

trait GeneratorUtils {
  var currentClass: ClassDefn
  var currentMethod: MethodDefn

  case class Location(reg: String, offset: Int) {
    lazy val deref = s"[$reg+$offset]"
  }

  // Get the memory location of a variable
  def varLocation(name: String, scope: Scope) = {
    // TODO: this fails for non-static fields
    if (currentMethod != null && scope.isLocalScope(name)) {
      val params = currentMethod.params.length

      // `this` is always the first thing pushed
      val stackOffset = scope.localVarStackIndex(name)

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

  def thisLocation = {
    // TODO: might fuck up for initializers
    val params =
      if (currentMethod != null)
        currentMethod.params.length
      else 0
    val offset = (params + 2) * 4
    Location("ebp", offset)
  }

  // Location of a reference's member
  def memLocation(m: Member) = {
    val t = m.lhs.t
    t match {
      case _: ArrayDefn =>
        // Must be the `length` field
        Location("ebx", 4)

      case c: ClassDefn =>
        val c = t.asInstanceOf[ClassDefn]
        val offset = c.getFieldIndex(m.rhs.name)

        // TODO: broken if not in ebx, but should always be
        Location("ebx", offset * 4)
    }
  }

  // Eager/lazy and or logic operators
  def logical(lhs: Expression, rhs: Expression, op: String, eager: Boolean) = {
    if (eager) {
      // Eager case is easy, just compute both and op them
      emit(lhs)
      Target.text.emit("push ebx")
      emit(rhs)
      Target.text.emit(
        "pop ecx",
        s"$op ebx, ecx"
      )
    } else {
      // Lazy case is harder, first compute lhs, see if it is equal to
      // shortValue, otherwise return value of rhs
      val shortValue = op match {
        case "and" => 0
        case "or"  => 1
        // explicitly missing a default case
      }

      val doneL = AnonLabel()
      emit(lhs)
      Target.text.emit(
        s"mov ecx, $shortValue",
        "cmp ebx, ecx",
        s"je $doneL"
      )
      emit(rhs)
      Target.text.emit(doneL)
    }
  }

  def unboxNumeric(ghs: Expression) = {
    emit(ghs)

    val t = ghs.et.r
    // TODO: this doesn't work because t.classId isn't the same as the
    // runtime classId... SMH
    if (Runtime.numericBoxes contains Runtime.lookup(t)) {
      val c = t.asInstanceOf[ClassDefn]
      val offset = c.getFieldIndex("value")
      val loc = Location("ebx", offset * 4).deref

      Target.text.emit(
        "; unbox numeric",
        s"mov ebx, $loc"
      )
    }
  }

  def arithmetic(b: BinOp, op: String) = {
    unboxNumeric(b.lhs)
    Target.text.emit("push ebx")
    unboxNumeric(b.rhs)

    if (op == "idiv") {
      val except = NamedLabel("_exception")
      val onwards = AnonLabel("div_safe")

      Target.file.reference(except)
      Target.text.emit(
        "cmp ebx, 0",
        s"jne $onwards",
        s"call $except",
        onwards,
        "mov edx, 0",
        "pop eax",
        s"$op ebx"
        )
    } else {
      Target.text.emit(
        "pop ecx",
        s"$op ecx, ebx",
        "mov ebx, ecx"
      )
    }
  }

  // Compare ebx to ecx, use jmpType to decide how they compare
  def cmpHelper(lhs: Expression, rhs: Expression, jmpType: String) = {
    val afterwards = AnonLabel()
    val falseCase = AnonLabel()

    emit(rhs)
    Target.text.emit("push ebx")
    emit(lhs)
    Target.text.emit(
      "pop ecx",
      "cmp ebx, ecx",
      s"jn$jmpType $falseCase",
      "mov ebx, 1",
      s"jmp $afterwards",
      falseCase,
      "mov ebx, 0",
      afterwards
      )
  }

  def idxHelper(idx: Index) = {
    val except = NamedLabel("_exception")
    Target.file.reference(except)
    val otherwise = AnonLabel("idx_ok")

    emit(idx.rhs)
    Target.text.emit("push ebx")
    emit(idx.lhs)
    Target.text.emit(
      "pop eax",
      "cmp eax, [ebx+4]",
      s"jl $otherwise",
      s"call $except",
      otherwise
    )
  }

  def emit(v: Visitable): Unit
}
