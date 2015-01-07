package juicy.source.tokenizer

object CharDFA {
  
  def matchSingleChar(stream: CharStream, end: Option[Char] = None): Option[Char] = {
    if (!end.isEmpty && stream.cur == end.get) {
      println("Read end char")
      None
    } else if (stream.cur == '\\') {
      stream.next()
      println("Read: " + stream.cur)
      stream.cur match {
        case 'b' => Some('\b')
        case 'f' => Some('\f')
        case 'n' => Some('\n')
        case 'r' => Some('\r')
        case 't' => Some('\t')
        case '\\' => Some('\\')
        case '\'' => Some('\'')
        case '\"' => Some('\"')
        case x if x.isDigit => {
           val is = stream.takeWhile(_.isDigit)
           Some(is.toInt.toChar)
        }
        case _ => {
          println("Invalid escape:" + stream.cur.toInt)
          None
        }
      }
    } else {
      Some(stream.cur)
    }
  }
  
  
  def matchChar(stream: CharStream) : Token = {
    stream.next()
    val ch = matchSingleChar(stream, Some('\''))
    stream.next()
    val end = stream.cur
    stream.next()
    if (ch.isEmpty || end != '\'') {
        new Token.Invalid()
    } else {
        new Token.CharLiteral(ch.get)
    }
  }
  
  def matchString(stream: CharStream): Token = {
    stream.next()
    var failed = false
    var chs = ""
    while (!failed && stream.cur != '\"') {
      val ch = matchSingleChar(stream)
      if(ch.isEmpty) {
        failed = true
      } else {
        chs += ch.get
      }
      stream.next()
    }
    stream.next()
    if (failed) {
      new Token.Invalid()
    } else {
      new Token.StringLiteral(chs)
    }
  }
}