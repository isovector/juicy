package juicy.source.tokenizer

// Mimic the semantics of CharStream
class TokenStream(source: String, fname: String = "<string>") {
  val tokenizer = new Tokenizer(source, fname)
  val previous = new scala.collection.mutable.Stack[Token]()
  val ahead = new scala.collection.mutable.Stack[Token]()
  val bookmarks = new scala.collection.mutable.Stack[Int]()

  var cur = tokenizer.next()
  def next() = {
    val prev = cur
    previous.push(cur)
    bookmarks.push(bookmarks.pop() + 1)
    cur =
      if (ahead.length == 0)
        tokenizer.next()
      else ahead.pop()
    prev
  }

  def rewind() = {
    ahead.push(cur)
    bookmarks.push(bookmarks.pop() - 1)
    cur = previous.pop()
  }

  def setBacktrace() = {
    bookmarks.push(0)
  }

  def unsetBacktrace() = {
    bookmarks.pop()
  }

  def backtrack() = {
    val back = bookmarks.pop()
    (0 to (back - 1)).foreach(_ => rewind())
  }

  setBacktrace()

  def takeWhile(p: Token => Boolean): Seq[Token] = {
    val results = new collection.mutable.MutableList[Token]()
    while (p(cur)) {
      results += cur
      next()
    }

    results.toList
  }
}
