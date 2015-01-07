package juicy.source.tokenizer

import juicy.source.SourceLocation

class Tokenizer(input: String) {
  val source = new CharStream(input)

  def cur: Char = source.cur
  def curLocation = new SourceLocation("<source>", source.line, source.col)

  def next(): Token = {
    source.eatSpace()
    val loc = curLocation

    if (cur.isDigit) {
      val ipart = source.takeWhile(_.isDigit)
      new IntLiteral(ipart.toInt).setFrom(curLocation)
    } else {
      new BoolLiteral(false)
    }
  }
}



