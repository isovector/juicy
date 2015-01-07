package juicy.source.tokenizer

// Mimic the semantics of CharStream
class TokenStream(source: String) {
  val tokenizer = new Tokenizer(source)

  var cur = tokenizer.next()
  def next() = {
    cur = tokenizer.next()
  }

  def takeWhile(p: Token => Boolean): Seq[Token] = {
    val results = new collection.mutable.MutableList[Token]()
    while (p(cur)) {
      results += cur
      next()
    }

    results.toList
  }
}
