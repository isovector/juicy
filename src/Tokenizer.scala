package juicy.source.tokenizer

import juicy.source.SourceLocation

class Tokenizer(input: String) {
  val source = new CharStream(input)

  def cur: Char = source.cur
  def curLocation = new SourceLocation("<source>", source.line, source.col)

  // These should be listed from longest to shortest, to ensure that we don't
  // do subtring matching
  val operators = List("==", ">=", "<=", ">", "<", "!=", "&&", "||", "%",
                        "+", "-", "*", "/", "&", "|", "!", "[", "]")
  val modifiers = List("public", "protected", "static", "extern", "final",
                        "abstract", "native")
  val keywords  = List("if", "for", "while", "class", "override", "new",
                        "return", "import", "package", "interface", "extends",
                        "implements")
  val single: Map[Char, Unit => Token] = Map(
    ';' -> (Unit => new Terminator()),
    '(' -> (Unit => new LParen()),
    ')' -> (Unit => new RParen()),
    '{' -> (Unit => new LBrace()),
    '}' -> (Unit => new RBrace()),
    ',' -> (Unit => new Comma())
  )

  def next(): Token = {
    source.eatSpace()
    val loc = curLocation
    nextImpl().setFrom(loc)
  }

  private def nextImpl(): Token = {
    if (single.contains(cur)) {
      val result = single(cur)()
      source.next()
      result
    } else if (cur.isDigit) {
      // Try to match a int literal first because they're easy
      val ipart = source.takeWhile(_.isDigit)
      new IntLiteral(ipart.toInt)
    } else if (cur == '\'') {
      // TODO: Match a char literal
      new CharLiteral('\0')
    } else if (cur == '\"') {
      // TODO: Match a string literal
      new StringLiteral("")
    } else {
      // Match an operator
      operators.find(op => source.matchExact(op)).map { op =>
        source.eatExact(op)
        new Operator(op)
      }

      // Match a keyword or identifier
      .getOrElse {
        val word = source.takeWhile(c => c.isLetterOrDigit ||
                                      c == '_' || c == '$')

        if (modifiers.contains(word)) {
          new Modifier(word)
        } else if (keywords.contains(word)) {
          new Keyword(word)
        } else if (word == "true" || word == "false") {
          new BoolLiteral(word.toBoolean)
        } else if (word == "null") {
          new NullLiteral()
        } else if (word == "this") {
          new ThisLiteral()
        } else if (word == "instanceof") {
          new Operator("instanceof")
        } else {
          new Identifier(word)
        }
      }
    }
  }
}



