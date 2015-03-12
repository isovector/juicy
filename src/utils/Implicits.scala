package juicy.utils

import juicy.source.ast.TypeDefn
import juicy.source.tokenizer.SourceLocation
import juicy.utils.visitor._

trait CompilerError extends Throwable {
  val msg: String
  val from: SourceLocation

  override def toString() = s"$msg\n\tat $from"
}

object Implicits {
  type QName = Seq[String]

  case class RichSeq[A](underlying: Seq[A]) {
    def collectMap[B](f: (A) => B): Visited[Seq[B]] = {
      val (errors, result) = underlying.map { a =>
        try {
          Right(f(a))
        } catch {
          case e: CompilerError => Left(Seq(e))
        }
        }.partition {
          case Left(_) => true
          case _       => false
        }

        if (errors.length != 0)
          Left(errors.flatMap(l => Visited.unwrapLeft(l)))
        else
          Right(result.map(r => Visited.unwrapRight(r)))
    }
  }

  implicit def seq2RichSeq[A](underlying: Seq[A]): RichSeq[A] =
    new RichSeq(underlying)

  case class RichBool(underlying: Boolean) {
    def -->(other: => Boolean) = !underlying || other
    def !-->(other: => Boolean) = !(!underlying || other)
    def <->(other: => Boolean) = !(other ^ underlying)
  }

  implicit def boolToRich(underlying: Boolean) = new RichBool(underlying)

  case class SuburbanClassDefn(u: TypeDefn, fromPkg: Boolean)
  implicit def subToDomClass(u: SuburbanClassDefn) = u.u
}

