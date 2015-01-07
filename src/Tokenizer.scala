package juicy.source.tokenizer

import juicy.source.SourceLocation

class Tokenizer(input: String) {
  val source = new CharStream(input)

  def cur: Char = source.cur
  def curLocation = new SourceLocation("<source>", source.line, source.col)

  // These should be listed from longest to shortest, to ensure that we don't
  // do subtring matching
  val operators = List("==", ">=", "<=", "!=", "&&", "||",
                        "+", "-", "*", "/", "&", "|", "!", "(", ")", "[", "]",
                        "{", "}", ",")
  val modifiers = List("public", "protected", "static", "extern", "final",
                        "abstract", "native")
  val keywords  = List("if", "for", "while", "class", "override", "new",
                        "return", "import", "package", "interface", "extends",
                        "implements")

  def next(): Token = {
    source.eatSpace()
    val loc = curLocation
    nextImpl().setFrom(loc)
  }

  private def nextImpl(): Token = {
    if (cur.isDigit) {
      // Try to match a int literal first because they're easy
      val ipart = source.takeWhile(_.isDigit)
      new Token.IntLiteral(ipart.toInt)
    } else if (cur == '\'') {
      // TODO: Match a char literal
      new Token.CharLiteral('\0')
    } else if (cur == '\"') {
      // TODO: Match a string literal
      new Token.StringLiteral("")
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

        if (modifiers.contains(word)) {
          new Token.Modifier(word)
        } else if (keywords.contains(word)) {
          new Token.Keyword(word)
        } else if (word == "true" || word == "false") {
          new Token.BoolLiteral(word.toBoolean)
        } else if (word == "null") {
          new Token.NullLiteral()
        } else if (word == "this") {
          new Token.ThisLiteral()
        } else if (word == "instanceof") {
          new Token.Operator("instanceof")
        } else {
          new Token.Identifier(word)
        }
      }
    }
  }
}



