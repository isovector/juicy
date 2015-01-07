import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

import juicy.source.tokenizer._

class TokenizerSpec extends FlatSpec with ShouldMatchers {
  import juicy.source.tokenizer.Token._

  def check(tokenizer: Tokenizer, expected: Token*) = {
    expected.toList.foreach { token =>
      tokenizer.next() should be === token
    }
  }

  "Tokenizer" should "match easy literals" in {
    check(
      new Tokenizer("15 true false null this"),
        new IntLiteral(15),
        new BoolLiteral(true),
        new BoolLiteral(false),
        new NullLiteral(),
        new ThisLiteral())
  }

  it should "differentiate between modifiers, keywords and identifiers" in {
    check(
      new Tokenizer("public class Main instanceof"),
        new Modifier("public"),
        new Keyword("class"),
        new Identifier("Main"),
        new Operator("instanceof"))
  }

  it should "lex arithmetic expressions" in {
    check(
      new Tokenizer("-2*x+87%x-(x/7)"),
        new Operator("-"),
        new IntLiteral(2),
        new Operator("*"),
        new Identifier("x"),
        new Operator("+"),
        new IntLiteral(87),
        new Operator("%"),
        new Identifier("x"),
        new Operator("-"),
        new LParen(),
          new Identifier("x"),
          new Operator("/"),
          new IntLiteral(7),
        new RParen())
  }

  it should "recognize boolean operators" in {
    check(
      new Tokenizer("< > <= >= == != && ||"),
        new Operator("<"),
        new Operator(">"),
        new Operator("<="),
        new Operator(">="),
        new Operator("=="),
        new Operator("!="),
        new Operator("&&"),
        new Operator("||"))
  }

  it should "lex eager boolean operators" in {
    check(
      new Tokenizer("& | !"),
        new Operator("&"),
        new Operator("|"),
        new Operator("!"))
  }

  it should "lex a real method" in {
    check(
      new Tokenizer("public boolean m(boolean x) { return (x && true) || x; }"),
        new Modifier("public"),
        new Identifier("boolean"),
        new Identifier("m"),
        new LParen(),
          new Identifier("boolean"),
          new Identifier("x"),
        new RParen(),
        new LBrace(),
          new Keyword("return"),
          new LParen(),
            new Identifier("x"),
            new Operator("&&"),
            new BoolLiteral(true),
          new RParen(),
          new Operator("||"),
          new Identifier("x"),
          new Terminator(),
        new RBrace())
  }
}
