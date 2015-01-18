package juicy.utils

import juicy.source.tokenizer.SourceLocation

trait CompilerError extends Throwable {
  val msg: String
  val from: SourceLocation

  override def toString() = s"$msg\n\tat $from"
}

object Implicits {
  type QName = Seq[String]

  case class RichSeq[T](underlying: Seq[T]) {
    def last: T = underlying(underlying.length - 1)
  }

  implicit def seq2RichSeq[T](underlying: Seq[T]): RichSeq[T] =
    new RichSeq(underlying)

  case class RichBool(underlying: Boolean) {
    def -->(other: => Boolean) = !underlying || other
    def !-->(other: => Boolean) = !(!underlying || other)
    def <->(other: => Boolean) = !(other ^ underlying)
  }

  implicit def boolToRich(underlying: Boolean) = new RichBool(underlying)
}

