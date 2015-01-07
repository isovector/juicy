import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

import juicy.source.tokenizer._

class CharStreamSpec extends FlatSpec with ShouldMatchers {
  "CharStream" should "be able to match strings exactly" in {
    val stream = new CharStream("abcdefghijk")
    stream.matchExact("abcdefgh") should be === true
    stream.eatExact("abcdefgh")
    stream.cur should be === 'i'
  }
}
