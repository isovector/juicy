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
        t match {
          case c: ClassDefn =>
            if (c.isClass) {
              throwIf(s"Class `$name` extends an interface or primitive") {
                resolve(c.extnds).exists(e => e match {
                  case cx: ClassDefn => cx.isInterface
                  case _ => true
                })
              }

              throwIf(s"Class `$name` implements a class or primitive") {
                resolve(c.impls).exists(im => im match {
                  case cx: ClassDefn => cx.isClass
                  case _ => true
                })
              }

              throwIf(s"Class `$name` extends a final class") {
                resolve(c.extnds).exists(x => x match {
                  case cx: ClassDefn => check(cx.mods, FINAL)
                  case _ => false
                })
              }

              throwIf(
                  s"Class `$name` implements an interface multiple times") {
                val impls = resolve(c.impls)

                impls.distinct != impls
              }

              throwIf(
                  s"Class `$name` extends a type without a default constructor") {
                !c.extnds.isEmpty &&
                !resolve(c.extnds).exists { superc =>
                  superc
                    .methods
                    .filter(_.isCxr)
                    .exists(_.params.isEmpty)
                }
              }
            } else {
              throwIf(s"Interface `$name` extends a class or primitive") {
                resolve(c.impls).exists(i => i match {
                  case c: ClassDefn => c.isClass
                  case _ => true
                })
              }

              throwIf(
                  s"Interface `$name` extends an interface multiple times") {
                val extnds = resolve(c.extnds)

                extnds.distinct != extnds
              }
            }

            c.allInterfaces.foreach { impl =>
              impl.inheritedMethods.foreach { method =>
                val matched = t.inheritedMethods.find(_ ~== method)
                if (matched.isDefined) {
                  val matching = matched.get
                  val sameMethod = matching == method
                  val sameTname = matching.tname == method.tname
                  val myMethod = c.methods contains matching
                  val lessAccessible = check(matching.mods, PROTECTED)

                  val myName = s"$name.${method.name}"
                  val iName = s"${impl.name}.${method.name}"

                  if (!sameTname) {
                    throw KnowerError(
                      s"`$myName` replaces `$iName` with a different type", t)
                  }

                  if (!sameMethod && myMethod && lessAccessible) {
                    throw KnowerError(
                      s"`$myName` is less visible than inherited `$iName`", t)
                  }

                  if (!sameMethod && !myMethod && lessAccessible &&
                    !check(matching.mods, ABSTRACT)) {
                    throw KnowerError(
                      s"Class `$name` inherits method which is less visible than `${iName}`", t)
                  }
                } else if(!check(c.mods, ABSTRACT)) {
                  throw KnowerError(
                    s"Class `$name` is missing method `${method.name}`", t)
                }
              }
            }

            throwIf(s"Class `$name` implements mutually-exlusive interfaces") {
              val methodsBySignatures =
                c.allInterfaces
                  .flatMap(_.methods)
                  .groupBy(_.signature)

              methodsBySignatures.exists { case (_, methods) =>
                val returnType = methods.head.tname
                methods.exists(_.tname != returnType)
              }
            }

            throwIf(s"Class `$name` has non-unique methods") {
              val methods = c.methods.map(_.signature)
              methods != methods.distinct
            }


            c.hidesMethods.foreach { hidden =>
              val name = hidden.name
              val hider = t.inheritedMethods.find(_.name == name).get

              val hideMods = hider.mods
              val hiddenMods = hidden.mods

              throwIf(s"Non-static method `$name` hides a static method") {
                !check(hideMods, STATIC) && check(hiddenMods, STATIC)
              }

              throwIf(s"Static method `$name` hides a non-static method") {
                check(hideMods, STATIC) && !check(hiddenMods, STATIC)
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

            if (!c.isInterface && !check(c.mods, ABSTRACT)) {
              // Ensure no methods are abstract
              c.inheritedMethods.foreach { method =>
                throwIf(s"Non-abstract class `$name` contains abstract methods") {
                  check(method.mods, ABSTRACT)
                }
              }
            }
          case _ =>
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

