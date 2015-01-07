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

    if (cur.isDigit) {
      // Try to match a int literal first because they're easy
      val ipart = source.takeWhile(_.isDigit)
      new IntLiteral(ipart.toInt).setFrom(curLocation)
    } else if (cur == '\'') {
      // TODO: Match a char literal
      new CharLiteral('\0').setFrom(curLocation)
    } else if (cur == '\"') {
      // TODO: Match a string literal
      new StringLiteral("").setFrom(curLocation)
    } else {
      // Match an operator
      operators.find(op => source.matchExact(op)).map { op =>
        source.eatExact(op)
        new Operator(op).setFrom(curLocation)
      }

      // Match a keyword or identifier
      .getOrElse {
        val word = source.takeWhile(c => c.isLetterOrDigit ||
                                      c == '_' || c == '$')

        if (modifiers.contains(word)) {
          new Modifier(word).setFrom(curLocation)
        } else if (keywords.contains(word)) {
          new Keyword(word).setFrom(curLocation)
        } else if (word == "true" || word == "false") {
          new BoolLiteral(word.toBoolean).setFrom(curLocation)
        } else if (word == "null") {
          new NullLiteral()
        } else if (word == "this") {
          new ThisLiteral()
        } else if (word == "instanceof") {
          new Operator("instanceof")
        } else {
          throw new Exception()
        }
      }
    }
  }
}



