package juicy.utils

object Implicits {
  case class RichSeq[T](underlying: Seq[T]) {
    def last: T = underlying(underlying.length - 1)
  }

  implicit def seq2RichSeq[T](underlying: Seq[T]): RichSeq[T] =
    new RichSeq(underlying)
}

