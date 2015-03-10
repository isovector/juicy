import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

import juicy.source.ambiguous._
import juicy.source.ast._
import juicy.source.PackageTree
import juicy.source.parser._
import juicy.source.resolver._
import juicy.source.scoper._
import juicy.source.tokenizer._
import juicy.utils.CompilerError
import juicy.utils.visitor.VisitError

class AmbiguatorSpec extends FlatSpec with ShouldMatchers {
  def sexy(sources: String*): (Seq[FileNode], PackageTree) = {
    val srcs = sources.toList ++ ResolverSpec.stdlib
    val files = srcs.map { source =>
      new Parser(new TokenStream(source)).parseFile()
    }

    val pkgtree = Resolver(files)
    HardlyKnower(pkgtree)
    files.foreach(Hashtag360NoScoper(_))
    (Sexuality(files, pkgtree), pkgtree)
  }

  "Ambiguator" should "resolve static accesses" in {
    val (transformed, pkgtree) = sexy(
      """
      class X {
        public X() {
          java.lang.Object.call().member = 5;
          Object.call().member = true;
        }
      }
      """
      )

    val stmnts =
      transformed(0)
        .classes(0)
        .methods(0)
        .body
        .get
        .asInstanceOf[BlockStmnt]
        .body

    val access =
      Member(
        Call(
          StaticMember(
            pkgtree.getType(Seq("java", "lang", "Object")).get,
            Id("call")
          ),
          Seq()
        ),
        Id("member")
      )

    stmnts(0)
      .asInstanceOf[ExprStmnt]
      .expr should be ===
        Assignment(
          access,
          IntVal(5)
        );

    stmnts(1)
      .asInstanceOf[ExprStmnt]
      .expr should be ===
        Assignment(
          access,
          BoolVal(true)
        );
  }
}
