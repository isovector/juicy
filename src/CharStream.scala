package juicy.source.tokenizer

class CharStream(source: String) {
  var line = 1
  var col = 1

  private var stream = (source + '\0').toList

  def cur = stream.head

  def next() = {
    cur match {
      case '\n' =>
        line += 1
        col = 1

      case _ =>
        col += 1
    }

    stream = stream.tail
  }

  def takeWhile(p: Char => Boolean): String = {
    val result = stream.takeWhile(p)
    result.foreach(_ => next())
    result.mkString("")
  }

  def matchExact(exact: String) = {
    stream.zip(exact.toList).find(x => x._1 != x._2).isEmpty
  }

  def eatExact(exact: String) = {
    // TODO: do we want to ensure it matches first?
    exact.foreach(_ => next())
  }

  def eatSpace() = {
    while (cur.isWhitespace) next()
  }

  def isEnd = cur == '\0'
}

