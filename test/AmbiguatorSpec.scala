import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

import juicy.source.ambiguous._
import juicy.source.ast._
import juicy.source.parser._
import juicy.source.resolver._
import juicy.source.tokenizer._
import juicy.utils.CompilerError
import juicy.utils.visitor.VisitError

class AmbiguatorSpec extends FlatSpec with ShouldMatchers {
  def sexy(sources: String*) = {
    val srcs = sources.toList ++ ResolverSpec.stdlib
    val files = srcs.map { source =>
      new Parser(new TokenStream(source)).parseFile()
    }

    HardlyKnower(Resolver(files))
    Sexuality(files)
  }

  "Ambiguator" should "successfully compile" in {
    sexy(
      """
      class X {
        public X() {
          java.lang.Object();
        }
      }
      """
      )
    1 should be === 1
  }
}
