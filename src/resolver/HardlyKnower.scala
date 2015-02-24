package juicy.source.resolver

import juicy.source.ast._
import juicy.source.ast.Modifiers._
import juicy.source.PackageTree
import juicy.utils.CompilerError
import juicy.utils.Implicits._
import juicy.utils.visitor._


case class KnowerError(msg: String, in: Visitable)
    extends CompilerError {
  val from = in.from
}

object HardlyKnower {
  private def check(which: Modifiers.Value, flag: Modifiers.Value) =
    (which & flag) == flag

  def apply(pkgtree: PackageTree): Boolean = {
    val types = pkgtree.tree.toSeq.map(_._2).flatten

    try {
      types.collectMap { t =>
        implicit val implType = t
        val name = t.name

        if (t.isClass) {
          throwIf(s"Class `$name` extends an interface") {
            resolve(t.extnds).exists(_.isInterface)
          }

          throwIf(s"Class `$name` implements a class") {
            resolve(t.impls).exists(!_.isInterface)
          }

          throwIf(s"Class `$name` extends a final class") {
            resolve(t.extnds).exists(x => check(x.mods, FINAL))
          }

          throwIf(
              s"Class `$name` implements an interface multiple times") {
            val impls = resolve(t.impls)

            impls.distinct != impls
          }
        } else {
          throwIf(s"Interface `$name` extends a class") {
            resolve(t.impls).exists(_.isClass)
          }

          throwIf(
              s"Interface `$name` extends an interface multiple times") {
            val extnds = resolve(t.extnds)

            extnds.distinct != extnds
          }
        }

        throwIf(s"Class `$name` does not implement what it promises") {
          // Go through each implements, and
          t.allInterfaces.exists { impl =>
            impl.allMethods.exists { method =>
              // Ensure the type has a method with the same signature
              // This block returns whether or not the method IS constrained
              !(t.allMethods.find(_ ~== method) match {
                case Some(matching) =>
                  // And that this isn't the SAME method (by comparing
                  // inherited members), and that it's accessible
                  (matching == method || !check(matching.mods, PROTECTED)) &&
                  (matching.tname == method.tname)

                // Abstract classes don't need to implement the whole interface
                case _ => check(t.mods, ABSTRACT)
              })
            }
          }
        }

        throwIf(s"Class `$name` implements mutually-exlusive interfaces") {
          val methodsBySignatures =
            t.allInterfaces
              .flatMap(_.methods)
              .groupBy(_.signature)

          methodsBySignatures.exists { case (_, methods) =>
            val returnType = methods.head.tname
            methods.exists(_.tname != returnType)
          }
        }

        throwIf(s"Class `$name` has non-unique methods") {
          val methods = t.methods.map(_.signature)
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

  def resolve(t: Typename) = t.resolved.get
  def resolve(t: Seq[Typename]) = t.map(_.resolved.get)

  def throwIf(msg: => String)(predicate: => Boolean)(implicit whence: Visitable) = {
    if (predicate)
      throw KnowerError(msg, whence)
  }
}

