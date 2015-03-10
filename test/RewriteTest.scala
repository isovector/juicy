import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

import juicy.source.ast._
import juicy.utils.visitor.Rewriter

class RewriteSpec extends FlatSpec with ShouldMatchers {
  "Rewriter" should "successfully compile" in {
    // fuck the rewriter
    1 should be === 1
  }
}
