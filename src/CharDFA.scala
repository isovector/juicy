package juicy.source.tokenizer

class CharDFA {
  // TODO: actually parse a char
  def matchSingleChar(stream: CharStream, end: Option[Char] = None): Option[Char] = {
    if (!end.isEmpty && stream.cur == end) {
      None
    } else if (stream.cur == '\\') {
      stream.next()
      stream.cur match {
        case 'b' => Some('\b')
        case 'f' => Some('\f')
        case 'n' => Some('\n')
        case 'r' => Some('\r')
        case 't' => Some('\t')
        case _ => {
           val is = stream.takeWhile(_.isDigit)
           if (is.isEmpty) {
               None
           } else {
               Some(is.toInt.toChar)
           }
        }
      }
    } else {
      Some(stream.cur)
    }
  }
  
  
  def matchChar(stream: CharStream) : Token = {
    val ch = matchSingleChar(stream, Some('\''))
    if (ch.isEmpty) {
        new Token.Invalid()
    } else {
        new Token.CharLiteral(ch.get)
    }
  }
  def matchString(stream: CharStream): Token = {
    var failed = false
    var chList = List[Char]();
    while (!failed && stream.cur != '\"') {
      val ch = matchSingleChar(stream)
      if(ch.isEmpty) {
        failed = true
      } else {
        chList ::= ch.get
      }
    }
    if (failed) {
      Token.Invalid()
    } else {
      Token.StringLiteral(chList.mkString(""))
    }
  }
}