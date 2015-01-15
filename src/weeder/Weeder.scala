package juicy.source.weeder

import juicy.source.ast._
import juicy.source.ast.Modifiers._
import juicy.utils.visitor._

object Weeder {
    
  // HACK HACK HACK HACK HACK
  var checkFileName = true
    
  def check(which: Modifiers.Value, flag: Modifiers.Value) =
    (which & flag) == flag

  def apply(node: Visitable): Boolean = {
      // TODO: still missing:
      // A class/interface must be declared in a .java file with the same base name as the class/interface.
   

    // Check modifiers for sanity
    node.visit((a: Boolean, b: Boolean) => a && b)
    { (self, context) =>
      self match {
        case Before(ClassDefn(name, mods, extnds, impls, fields, cxrs, methods, isInterface)) =>
          
          (!checkFileName || {
            val fname = node.originalToken.from.file.split('/').last
            fname.endsWith(".joos") && fname.slice(0, fname.length - ".joos".length) == name
          }) &&
          // A class cannot be both abstract and final.
          ((!check(mods, ABSTRACT) || !check(mods, FINAL)) &&

          // Extends and implements may not be arrays
          (extnds.isEmpty || !extnds.get.isArray) &&
          ((true /: impls)(_ && !_.isArray)) && 
          
          (if (isInterface) {
            // An interface cannot contain fields or constructors.
            fields.isEmpty && cxrs.isEmpty &&
            // An interface method cannot be static, final, or native.
            ((true /: methods)({(prev, m) =>
                prev && !check(m.mods, STATIC) && !check(m.mods, FINAL) && !check(m.mods, NATIVE) &&
                // An interface method cannot have a body.
                m.body.isEmpty
            }))
          } else {
              // Every class must contain at least one explicit constructor.
              !cxrs.isEmpty
          }))
          


        case Before(MethodDefn(_, mods, _, _, body)) =>
          // has a body if and only if it is neither abstract nor native.
          ((body.isEmpty || !(check(mods, ABSTRACT) || check(mods, NATIVE))) &&

          // abstract method cannot be static or final.
          (!check(mods, ABSTRACT) ||
            !(check(mods, STATIC) || check(mods, FINAL))) &&

          // A static method cannot be final.
          (!check(mods, STATIC) || !check(mods, FINAL)) &&

          // A native method must be static.
          (!check(mods, NATIVE) || check(mods, STATIC)))


        case Before(VarStmnt(_, mods, tname, _)) =>
          // The type void may only be used as the return type of a method.
          ((tname.toString != "void") &&

          // No field can be final.
          (context.head match {
            case _: ClassDefn => !check(mods, FINAL)
            case _            => true
          }))
        
        // A method or constructor must not contain explicit this() or super() calls.
        case Before(Call(ThisVal(), _)) => false
        case Before(Call(SuperVal(), _)) => false

        case _ => true
      }
    }
  }
}

