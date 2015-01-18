package juicy.source.weeder

import juicy.source.ast._
import juicy.source.ast.Modifiers._
import juicy.utils.CompilerError
import juicy.utils.Implicits._
import juicy.utils.visitor._

case class WeederError(msg: String, in: Visitable)
    extends CompilerError {
  val from = in.from
}

object Weeder {
  // HACK HACK HACK HACK HACK
  object debug {
    var checkFileName = true
  }

  private def check(which: Modifiers.Value, flag: Modifiers.Value) =
    (which & flag) == flag

  private def inInterface(context: Seq[Visitable]) = {
    (false /: context) { (last, node) =>
      last || (node match {
        case ClassDefn(_, _, _, _, _, _, _, isInterface) => isInterface
        case _                                           => false
      })
    }
  }

  def apply(node: Visitable): Boolean = {
    // Check modifiers for sanity
    node.visit((a: Unit, b: Unit) => {})
    { (self, context) =>
      self match {
        case Before(me@ClassDefn(name, mods, extnds, impls, fields, cxrs, methods, isInterface)) =>
          val basename = {
            val fname = node.originalToken.from.file.split('/').last
            fname.takeWhile(x => x != '.')
          }

          // A class/interface must be declared in a .java file with the same
          // base name as the class/interface.
          if (debug.checkFileName !--> (basename == name)) {
            throw new WeederError(
              s"Found class `$name` in file `$basename.joos`", me)
          }

          // A class cannot be both abstract and final.
          if (check(mods, ABSTRACT) !--> !check(mods, FINAL)) {
            throw new WeederError(
              s"Class `$name` declared both abstract and final", me)
          }

          // Extends may not be an array
          if (!extnds.isEmpty !--> !extnds.get.isArray) {
            throw new WeederError(s"Class `$name` extends an array", me)
          }

          // Implements may not be an array
          if (!(true /: impls)(_ && !_.isArray)) {
            throw new WeederError(s"Class `$name` implements an array", me)
          }

          if (isInterface) {
            // An interface cannot contain fields or constructors
            if (!(fields.isEmpty && cxrs.isEmpty)) {
              throw new WeederError(
                s"Interface `$name` contains fields or constructors",
                me)
            }
          }

          if (!isInterface !--> !cxrs.isEmpty) {
            throw new WeederError(
              s"Class `$name` must contain an explicit constructor", me)
          }


        case Before(me@MethodDefn(name, mods, _, _, body)) =>
          if (inInterface(context)) {
            // An interface method cannot have a body.
            if (!body.isEmpty) {
              throw new WeederError(
                s"Interface method `$name` has a body", me)
            }

            if (check(mods, STATIC) || check(mods, FINAL)
                || check(mods, NATIVE)) {
              throw new WeederError(
                s"Interface method `$name` is marked static, final or native",
                me)
            }
          } else {
            // has a body if and only if it is neither abstract nor native.
            if (!(body.isEmpty <->
                (check(mods, ABSTRACT) || check(mods, NATIVE)))) {
              throw new WeederError(
                s"Method `$name` must only have a body iff it is not abstract or native",
                me)
            }
          }

          // abstract method cannot be static or final.
          if (check(mods, ABSTRACT) !-->
              !(check(mods, STATIC) || check(mods, FINAL))) {
            throw new WeederError(
              s"Method `$name` may not be abstract and (static or final)", me)
          }

          // A static method cannot be final.
          if (check(mods, STATIC) !--> !check(mods, FINAL)) {
            throw new WeederError(
              s"Method `$name` may not be static and final", me)
          }

          // A native method must be static.
          if (check(mods, NATIVE) !--> check(mods, STATIC)) {
            throw new WeederError(
              s"Method `$name` must be marked static, since it is native", me)
          }


        case Before(me@VarStmnt(name, mods, tname, _)) =>
          // The type void may only be used as the return type of a method.
          if (tname.toString == "void") {
            throw new WeederError(
              "Type `void` may only be used for the return type of a method",
              me)
          }

          // No field can be final.
          if (context.head match {
            case _: ClassDefn => check(mods, FINAL)
            case _            => false
          }) {
            throw new WeederError(
              s"Field `$name` may not be marked final", me)
          }

        // A method or constructor must not contain explicit this() or super() calls.
        case Before(me@Call(ThisVal(), _)) =>
            throw new WeederError(
              s"Can't explicitly call this()", me)

        case Before(me@Call(SuperVal(), _)) =>
            throw new WeederError(
              s"Can't explicitly call super()", me)

        case _ => true
      }
    }.fold(
      l =>
        if (!debug.checkFileName)
          false
        else
          throw new VisitError(l),
      r => true
    )
  }
}

