package juicy.source.tokenizer

import juicy.source.SourceLocation

trait Token {
  def setFrom(loc: SourceLocation): Token = {
    // TODO: write loc into the token
    return this
  }
}

case class Keyword(value: String) extends Token
case class BoolLiteral(value: Boolean) extends Token
case class IntLiteral(value: Int) extends Token
case class CharLiteral(value: Char) extends Token
case class StringLiteral(value: String) extends Token
case class NullLiteral() extends Token
case class ThisLiteral() extends Token
case class Modifier(value: String) extends Token
case class Operator(value: String) extends Token
case class Identifier(value: String) extends Token

