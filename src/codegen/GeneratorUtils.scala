package juicy.codegen

import juicy.codegen.Implicits._
import juicy.source.ast._
import juicy.source.scoper.Scope
import juicy.utils.visitor._

trait GeneratorUtils {
  var currentClass: ClassDefn
  var currentMethod: MethodDefn

  case class Location(reg: String, offset: Int) {
    lazy val deref = s"[$reg+$offset]"
  }

  // Get the memory location of a variable
  def varLocation(name: String, scope: Scope) = {
    // TODO: this fails for non-static fields
    if (currentMethod != null) {
      val params = currentMethod.params.length

      // `this` is always the first thing pushed
      val stackOffset = scope.localVarStackIndex(name)

      // Check if our offset number is < num of params, if so, we are up the
      // stack, otherwise down
      val offset =
        if (stackOffset <= params)
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
    val params = currentMethod.params.length
    val offset = (params + 2) * 4
    Location("ebp", offset)
  }

  // Location of a reference's member
  def memLocation(m: Member) = {
    val t = m.lhs.t
    if (!t.isInstanceOf[ClassDefn]) {
      // TODO: help
      Target.text.emit("; array access mem location???")
      Location("ebx", 0)
    } else {
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

  def emit(v: Visitable): Unit
}
