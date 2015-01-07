package juicy.source.tokenizer

object CharDFA {
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
      println("Read end char")
      None
    } else if (stream.cur == '\\') {
      stream.next()
      stream.cur match {
        case x if charCodes contains x => {
            stream.next()
            Some(charCodes(x))
        }
        case x if x.isDigit => {
           val is = stream.takeWhile(_.isDigit)
           println(is)
           Some(is.toInt.toChar)
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
    if (ch.isEmpty || end != '\'') {
        println("ch: " + ch)
        println(ch.isEmpty)
        println("end: " + end.toInt)
        println(end == '\'')
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
    }
    stream.next()
    if (failed) {
      new Token.Invalid()
    } else {
      new Token.StringLiteral(chs)
    }
  }
}