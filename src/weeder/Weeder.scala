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
  object debug {
    // HACK HACK HACK HACK HACK
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

          if (check(mods, PUBLIC) <-> check(mods, PROTECTED)) {
            throw new WeederError(
              s"Class `$name` must be declared either public or protected", me)
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

          // Extends may not be primitive
          if (!extnds.isEmpty !--> !extnds.get.isPrimitive) {
            throw new WeederError(s"Class `$name` extends a primitive", me)
          }

          // Implements may not be an array
          if (!(true /: impls)(_ && !_.isArray)) {
            throw new WeederError(s"Class `$name` implements an array", me)
          }

          // Implements may not be a primitive
          if (!(true /: impls)(_ && !_.isPrimitive)) {
            throw new WeederError(s"Class `$name` implements a primitive", me)
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

          cxrs.map { cxr =>
            val cmods = cxr.mods
            if (check(cmods, FINAL) ||
                check(cmods, NATIVE) ||
                check(cmods, STATIC) || check(cmods, ABSTRACT))
              throw new WeederError(
                s"Constructor must only be marked public or private", me)
          }

          (fields ++ methods ++ cxrs).map { me =>
            val member = me.asInstanceOf[{
              val name: String
              val mods: Modifiers.Value }]
            val mname = member.name
            val mmods = member.mods

            if (check(mmods, PUBLIC) <-> check(mmods, PROTECTED)) {
              throw new WeederError(
                s"Member `$mname` must be marked either public or private", me)
            }
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

        case Before(me@Assignment(Cast(_, _), _)) =>
          throw new WeederError(
            s"Can't cast lhs of assignment operator", me)

        case Before(me@NewType(tname, _)) =>
          if (tname.isPrimitive)
            throw new WeederError(
              s"Cannont instantiate primitive type `$tname`", me)

        case Before(me: Typename) =>
          if (me.toString == "void")
            context.head match {
              case _: MethodDefn => // do nothing
              case _             =>
                throw new WeederError(
                  s"Type `void` may only be used as a function return type", me)
            }

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

