package juicy.source.tokenizer

class CharDFA {
  // TODO: actually parse a char
  def matchSingleChar(stream: CharStream, end: Char): Option[Char] = {
    if (stream.cur == end) {
      None
    } else if (stream.cur == '\\') {
      Some('\0')
    } else {
        Some('\'')
    }
  }
  
  
  def matchChar(stream: CharStream) : Token = {
    val ch = matchSingleChar(stream, '\'')
    if (ch.isEmpty) {
        new Token.Invalid()
    } else {
        new Token.CharLiteral(ch.get)
    }
  }
  def matchString(stream: CharStream): String = {
    ""
  }
}