import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

import juicy.source.tokenizer._

class TokenizerSpec extends FlatSpec with ShouldMatchers {
  import juicy.source.tokenizer.Token._

  "Tokenizer" should "match easy literals" in {
    val tokenizer = new Tokenizer("15 true false null this")

    tokenizer.next() should be === new IntLiteral(15)
    tokenizer.next() should be === new BoolLiteral(true)
    tokenizer.next() should be === new BoolLiteral(false)
    tokenizer.next() should be === new NullLiteral()
    tokenizer.next() should be === new ThisLiteral()
  }

  it should "differentiate between modifiers, keywords and identifiers" in {
    val tokenizer = new Tokenizer("public class Main instanceof")

    tokenizer.next() should be === new Modifier("public")
    tokenizer.next() should be === new Keyword("class")
    tokenizer.next() should be === new Identifier("Main")
    tokenizer.next() should be === new Operator("instanceof")
  }

  it should "lex arithmetic expressions" in {
    val tokenizer = new Tokenizer("-2*x+87%x-(x/7)")

    tokenizer.next() should be === new Operator("-")
    tokenizer.next() should be === new IntLiteral(2)
    tokenizer.next() should be === new Operator("*")
    tokenizer.next() should be === new Identifier("x")
    tokenizer.next() should be === new Operator("+")
    tokenizer.next() should be === new IntLiteral(87)
    tokenizer.next() should be === new Operator("%")
    tokenizer.next() should be === new Identifier("x")
    tokenizer.next() should be === new Operator("-")
    tokenizer.next() should be === new LParen()
    tokenizer.next() should be === new Identifier("x")
    tokenizer.next() should be === new Operator("/")
    tokenizer.next() should be === new IntLiteral(7)
    tokenizer.next() should be === new RParen()
  }

  it should "recognize boolean operators" in {
    val tokenizer = new Tokenizer("< > <= >= == != && ||")

    tokenizer.next() should be === new Operator("<")
    tokenizer.next() should be === new Operator(">")
    tokenizer.next() should be === new Operator("<=")
    tokenizer.next() should be === new Operator(">=")
    tokenizer.next() should be === new Operator("==")
    tokenizer.next() should be === new Operator("!=")
    tokenizer.next() should be === new Operator("&&")
    tokenizer.next() should be === new Operator("||")
  }

  it should "lex eager boolean operators" in {
    val tokenizer = new Tokenizer("& | !")

    tokenizer.next() should be === new Operator("&")
    tokenizer.next() should be === new Operator("|")
    tokenizer.next() should be === new Operator("!")
  }

  it should "lex a real method" in {
    val tokenizer = new Tokenizer(
      "public boolean m(boolean x) { return (x && true) || x; }")

    tokenizer.next() should be === new Modifier("public")
    tokenizer.next() should be === new Identifier("boolean")
    tokenizer.next() should be === new Identifier("m")
    // TODO
  }
}
