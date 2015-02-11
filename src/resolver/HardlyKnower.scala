package juicy.source.resolver

import juicy.source.ast._
import juicy.source.ast.Modifiers._
import juicy.utils.CompilerError
import juicy.utils.Implicits._
import juicy.utils.visitor._


case class KnowerError(msg: String, in: Visitable)
    extends CompilerError {
  val from = in.from
}

object HardlyKnower {
//An interface must not be repeated in an implements clause, or in an extends clause of an interface. (JLS 8.1.4, dOvs simple constraint 3)
  private def check(which: Modifiers.Value, flag: Modifiers.Value) =
    (which & flag) == flag

  def apply(types: Seq[ClassDefn]): Boolean = {
    try {
      types.collectMap { t =>
        implicit val implType = t
        val name = t.name

        if (t.isClass) {
          throwIf(s"Class `$name` extends an interface") {
            resolved(t.extnds)(_.isInterface)
          }

          throwIf(s"Class `$name` implements a class") {
            resolved(t.impls)(_.exists(!_.isInterface))
          }

          throwIf(s"Class `$name` extends a final class") {
            resolved(t.extnds)(x => check(x.mods, FINAL))
          }
        } else {
          throwIf(s"Interface `$name` extends a class") {
            resolved(t.impls)(_.exists(_.isClass))
          }
        }

        throwIf(s"Class `$name` has non-unique methods") {
          val methods = (t.methods ++ t.cxrs).map(_.signature)
          methods != methods.distinct
        }

        t.hidesMethods.foreach { hidden =>
          val name = hidden.name
          val hider = t.allMethods.find(_.name == name).get

          val hideMods = hider.mods
          val hiddenMods = hidden.mods

          throwIf(s"Non-static method `$name` hides a static method") {
            !check(hideMods, STATIC) && check(hiddenMods, STATIC)
          }

          throwIf(s"Protected method `$name` hides a public method") {
            check(hideMods, PROTECTED) && check(hiddenMods, PUBLIC)
          }

          throwIf(s"Method `$name` hides a final method") {
            check(hiddenMods, FINAL)
          }

          throwIf(s"Method `$name` hides a method with a different return type") {
            hider.tname != hidden.tname
          }
        }

        if (!t.isInterface && !check(t.mods, ABSTRACT)) {
          // Ensure no methods are abstract
          t.allMethods.foreach { method =>
            throwIf(s"Non-abstract class `$name` contains abstract methods") {
              check(method.mods, ABSTRACT)
            }
          }
        }
      }.fold(
        l => throw VisitError(l),
        r => true
      )
    } catch {
      case e: StackOverflowError =>
        throw KnowerError(
          "Cycle detected in class dependencies",
          types.head)
    }
  }

  def resolved(t: Typename)(predicate: ClassDefn => Boolean): Boolean =
    predicate(t.resolved.get)
  def resolved(t: Option[Typename])(predicate: ClassDefn => Boolean): Boolean =
    t.map { o => predicate(o.resolved.get) }.getOrElse(false)
  def resolved(t: Seq[Typename])(predicate: Seq[ClassDefn] => Boolean): Boolean =
    predicate(t.map(_.resolved.get))

  def throwIf(msg: => String)(predicate: => Boolean)(implicit whence: Visitable) = {
    if (predicate)
      throw KnowerError(msg, whence)
  }
}

