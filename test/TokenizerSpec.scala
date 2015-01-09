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

  it should "find commas" in {
    check(
      new Tokenizer(","),
        new Comma())
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

  it should "match char literals" in {
    check(
      new Tokenizer("'a' '\\n' '\\124' '\\\\'"),
        new CharLiteral('a'),
        new CharLiteral('\n'),
        new CharLiteral(124.toChar),
        new CharLiteral('\\')
    )
  }

  it should "also match string literals" in {
    check(
      new Tokenizer("\"sandy \\\"dandy\\\" maguire\""),
        new StringLiteral("sandy \"dandy\" maguire"))
  }

  it should "recognize single line comments" in {
    check(
      new Tokenizer("a // b \n // c \n d"),
        new Identifier("a"),
        new Identifier("d"))
  }

  it should "recognize multi line comments" in {
    check(
      new Tokenizer("a /* b \n c */ d"),
        new Identifier("a"),
        new Identifier("d"))
  }

  "TokenStream" should "be rewindable" in {
    import juicy.source.ParserUtils.strToToken

    val tokens = new TokenStream("1 2 3 4 5")
    tokens.cur should be === "1".asToken; tokens.next()
    tokens.cur should be === "2".asToken; tokens.next()
    tokens.setBacktrace()
    tokens.cur should be === "3".asToken; tokens.next()
    tokens.cur should be === "4".asToken; tokens.next()
    tokens.rewind()
    tokens.cur should be === "4".asToken; tokens.next()
    tokens.backtrack()
    tokens.cur should be === "3".asToken
  }
}
