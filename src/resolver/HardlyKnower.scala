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
//The hierarchy must be acyclic. (JLS 8.1.3, 9.1.2, dOvs well-formedness constraint 1)
//A class or interface must not contain (declare or inherit) two methods with the same signature but different return types (JLS 8.1.1.1, 8.4, 8.4.2, 8.4.6.3, 8.4.6.4, 9.2, 9.4.1, dOvs well-formedness constraint 3)
//A class that contains (declares or inherits) any abstract methods must be abstract. (JLS 8.1.1.1, well-formedness constraint 4)
//A nonstatic method must not replace a static method (JLS 8.4.6.1, dOvs well-formedness constraint 5)
//A method must not replace a method with a different return type. (JLS 8.1.1.1, 8.4, 8.4.2, 8.4.6.3, 8.4.6.4, 9.2, 9.4.1, dOvs well-formedness constraint 6)
//A protected method must not replace a public method. (JLS 8.4.6.3, dOvs well-formedness constraint 7)
/*A method must not replace a final method. (JLS 8.4.3.3, dOvs well-formedness constraint 9)*/
  private def check(which: Modifiers.Value, flag: Modifiers.Value) =
    (which & flag) == flag

  def apply(types: Seq[ClassDefn]) = {
    val result = types.collectMap { t =>
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

