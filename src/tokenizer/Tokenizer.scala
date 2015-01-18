package juicy.source.tokenizer

object Tokenizer {
  val singleChars: Map[Char, Unit => Token] = Map(
    '\0' -> (Unit => new Token.EOF()),
    ';'  -> (Unit => new Token.Terminator()),
    '('  -> (Unit => new Token.LParen()),
    ')'  -> (Unit => new Token.RParen()),
    '{'  -> (Unit => new Token.LBrace()),
    '}'  -> (Unit => new Token.RBrace()),
    ','  -> (Unit => new Token.Comma()),
    '.'  -> (Unit => new Token.Dot())
  )
}

class Tokenizer(input: String, fname: String = "<string>") {
  val source = new CharStream(input)

  def cur: Char = source.cur
  def curLocation = new SourceLocation(fname, source.line, source.col)

  // These should be listed from longest to shortest, to ensure that we don't
  // do subtring matching
  val primitives = List("void", "int", "char", "boolean", "short", "byte")
  val operators  = List("==", ">=", "<=", ">", "<", "!=", "&&", "||", "--", "%",
                        "+", "-", "*", "/", "&", "|", "!", "[", "]", "=")
  val modifiers  = List("public", "protected", "static", "extern", "final",
                        "abstract", "native")
  val keywords   = List("if", "for", "while", "class", "override", "new",
                        "return", "import", "package", "interface", "extends",
                        "implements", "goto", "private", "float", "double",
                        "long", "break", "continue")

  def eatComments() = {
    while (source.matchExact("//") || source.matchExact("/*")) {
      // eat single line comments
      if (source.matchExact("//")) {
        val curLine = source.line
        while (source.line == curLine) source.next()
      }

      // eat multiline comments
      else {
        while (!source.matchExact("*/")) source.next()
        source.eatExact("*/")
      }

      source.eatSpace()
    }
  }

  def next(): Token = {
    source.eatSpace()
    eatComments()

    val loc = curLocation
    nextImpl().setFrom(loc)
  }

  private def nextImpl(): Token = {
    if (cur >= 128) {
       new Token.Invalid()
    } else if (Tokenizer.singleChars.contains(cur)) {
      // Match single character tokens
      val result = Tokenizer.singleChars(cur)()
      source.next()
      result
    } else if (cur.isDigit) {
      // Try to match a int literal first because they're easy
      val ipart = source.takeWhile(_.isDigit)
      if (cur.isLetter || cur == '_') {
        new Token.Invalid()
      } else if (ipart.startsWith("0") && ipart.length > 1) {
        new Token.Invalid(Some("Octal Literals not supported"))
      } else {
        try {
          new Token.IntLiteral(ipart.toLong)
        } catch {
          case _: Throwable => new Token.Invalid(Some("Invalid integer literal " + ipart))
        }  
            
    }
    } else if (cur == '\'') {
      CharDFA.matchChar(source)
    } else if (cur == '\"') {
      CharDFA.matchString(source)
    } else {
      // Match an operator
      operators.find(op => source.matchExact(op)).map { op =>
        source.eatExact(op)
        new Token.Operator(op)
      }

      // Match a keyword or identifier
      .getOrElse {
        val word = source.takeWhile(c => c.isLetterOrDigit ||
                                      c == '_' || c == '$')

        word match {
          case ""                               => new Token.Invalid()
          case "true" | "false"                 => new Token.BoolLiteral(
                                                    word.toBoolean)
          case "instanceof"                     => new Token.Operator(
                                                    "instanceof")
          case "super"                          => new Token.SuperLiteral()
          case "null"                           => new Token.NullLiteral()
          case "this"                           => new Token.ThisLiteral()
          case _ if (modifiers.contains(word))  => new Token.Modifier(word)
          case _ if (keywords.contains(word))   => new Token.Keyword(word)
          case _ if (primitives.contains(word)) => new Token.Primitive(word)
          case _                                => new Token.Identifier(word)
        }
      }
    }
  }
}



