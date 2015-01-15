package juicy.utils

object Implicits {
  case class RichSeq[T](underlying: Seq[T]) {
    def last: T = underlying(underlying.length - 1)
  }

  implicit def seq2RichSeq[T](underlying: Seq[T]): RichSeq[T] =
    new RichSeq(underlying)
    
  case class RichBool(underlying: Boolean) {
    def -->(other: RichBool) = other.underlying || !underlying
    def <->(other: RichBool) = !(other.underlying ^ underlying) 
  }
  
  implicit def boolToRich(underlying: Boolean) = new RichBool(underlying)
}

