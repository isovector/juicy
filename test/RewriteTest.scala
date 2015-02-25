import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

import juicy.source.ast._
import juicy.utils.visitor.Rewriter

class RewriteSpec extends FlatSpec with ShouldMatchers {
  "Rewriter" should "successfully compile" in {
    val original =
      juicy.source.CompilerMain.parseFiles(
        Seq("joosc-test/J1_1_Cast_NamedTypeAsVariable.java")).head.classes(0)

    val rewritten =
      original.rewrite(Rewriter {
        case i@MethodDefn(name, mods, isCxr, tname, params, body) =>
          MethodDefn(
            "method",
            mods,
            isCxr,
            Typename(Seq("a", "a")),
            Seq(),
            None
          )

        case otherwise => otherwise
      })

    ClassDefn.suspendUniqueness {
      rewritten should be ===
        ClassDefn(
          "J1_1_Cast_NamedTypeAsVariable",
          Modifiers.PUBLIC,
          Seq(Typename(Seq("java", "lang", "Object"))),
          Seq(),
          Seq(),
          Seq(
            MethodDefn(
              "method",
              Modifiers.PUBLIC,
              true,
              Typename(Seq("a", "a")),
              Seq(),
              None
              ),
            MethodDefn(
              "method",
              Modifiers.PUBLIC | Modifiers.STATIC,
              false,
              Typename(Seq("a", "a")),
              Seq(),
              None
            )
          ))
    }
  }
}
