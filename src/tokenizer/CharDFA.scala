package juicy.source.tokenizer

object CharDFA {
    
  case class RichChar(underlying: Char) {
    def isOctalDigit() = underlying >= '0' && underlying < '8'
    def toDigit() : Int = underlying - '0'
  }
  implicit def charToRich(underlying: Char) = new RichChar(underlying)
  
  private val charCodes = Map[Char,Char](
      'b' -> '\b',
      'f' -> '\f',
      'n' -> '\n',
      'r' -> '\r',
      't' -> '\t',
      '\'' -> '\'',
      '\"' -> '\"',
      '\\' -> '\\'
  )
  def matchSingleChar(stream: CharStream, end: Option[Char] = None): Option[Char] = {
    if (!end.isEmpty && stream.cur == end.get) {
      None
    } else if (stream.cur == '\\') {
      stream.next()
      stream.cur match {
        case x if charCodes contains x => {
            stream.next()
            Some(charCodes(x))
        }
        case x if x.isOctalDigit => {
           var esc = x.asDigit
           stream.next()
           while(esc < 128 && stream.cur.isOctalDigit) {
             esc = esc * 8 + stream.cur.asDigit
             stream.next()
           }
           Some(esc.toChar)
        }
        case _ => {
          println("Invalid escape:" + stream.cur.toInt)
          None
        }
      }
    } else {
      val x = stream.cur
      stream.next()
      Some(x)
    }
  }


  def matchChar(stream: CharStream) : Token = {
    stream.next()
    val ch = matchSingleChar(stream, Some('\''))
    val end = stream.cur
    stream.next()
    if (ch.isEmpty) {
        new Token.Invalid(Some("Invalid escape in character Literal"))
    } else if (end != '\'') {
        new Token.Invalid(Some("Character Literal consists of multiple characters"))
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
    }
    stream.next()
    if (failed) {
      new Token.Invalid(Some("Invalid escape in string literal"))
    } else {
      new Token.StringLiteral(chs)
    }
  }
}
