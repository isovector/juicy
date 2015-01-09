package juicy.source

import juicy.ast._
import juicy.ast.AST._
import juicy.ast.Modifiers._

object Weeder {
  def check(which: Modifiers.Value, flag: Modifiers.Value) =
    (which & flag) == flag

  def apply(node: Node): Boolean = {
    // TODO: still missing:
      // All characters in the input program must be in the range of 7-bit ASCII (0 to 127).
      // A formal parameter of a method must not have an initializer.
      // A class/interface must be declared in a .java file with the same base name as the class/interface.
      // An interface cannot contain fields or constructors.
      // An interface method cannot be static, final, or native.
      // An interface method cannot have a body.
      // Every class must contain at least one explicit constructor.
      // No multidimensional array types or array creation expressions are allowed.
      // A method or constructor must not contain explicit this() or super() calls.


    // Check modifiers for sanity
    node.visit((a: Boolean, b: Boolean) => a && b)
    { (self, context) =>
      self match {
        case ClassDefn(_, mods, extnds, impls, _, _) =>
          // A class cannot be both abstract and final.
          ((!check(mods, ABSTRACT) || !check(mods, FINAL)) &&

          // Extends and implements may not be arrays
          (extnds.isEmpty || !extnds.get.isArray) &&
          ((true /: impls)(_ && !_.isArray)))


        case MethodDefn(_, mods, _, _, body) =>
          // has a body if and only if it is neither abstract nor native.
          ((body.isEmpty || !(check(mods, ABSTRACT) || check(mods, NATIVE))) &&

          // abstract method cannot be static or final.
          (!check(mods, ABSTRACT) ||
            !(check(mods, STATIC) || check(mods, FINAL))) &&

          // A static method cannot be final.
          (!check(mods, STATIC) || !check(mods, FINAL)) &&

          // A native method must be static.
          (!check(mods, NATIVE) || check(mods, STATIC)))


        case VarStmnt(_, mods, tname, _) =>
          // The type void may only be used as the return type of a method.
          ((tname.toString != "void") &&

          // No field can be final.
          (context.head match {
            case _: ClassDefn => !check(mods, FINAL)
            case _            => true
          }))


        case _ => true
      }
    } ((_, _) => true)
  }
}

