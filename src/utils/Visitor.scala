package juicy.utils.visitor

trait VisitOrder
case class Before(n: Visitable) extends VisitOrder
case class After(n: Visitable) extends VisitOrder

case class VisitError(errors: Seq[Throwable]) extends Throwable

trait Visitable {
  import juicy.source.tokenizer._
  var originalToken: Token = new Token.Invalid()
  def from = originalToken.from
  def children: Seq[Visitable]

  type Visited[T] = Either[Seq[Throwable], T]

  private def unwrapLeft[T](wrapped: Visited[T]): Seq[Throwable] = {
    wrapped match {
      case Left(unwrapped) => unwrapped
      case _               => Seq()
    }
  }

  private def unwrapRight[T](wrapped: Visited[T]): T = {
    wrapped match {
      case Right(unwrapped) => unwrapped
      case _                => throw new Exception("impossibruuuu")
    }
  }

  def visit[T]
      (fold: (T, T) => T)
      (func: (VisitOrder, Seq[Visitable]) => T): Visited[T] = {

    def lifted(a: Visited[T], b: Visited[T]): Visited[T] =
      if (a.isLeft || b.isLeft)
        Left(unwrapLeft(a) ++ unwrapLeft(b))
      else
        Right(fold(unwrapRight(a), unwrapRight(b)))

    visit(this, Seq())(lifted)(func)
  }

  def visit[T]
      (self: Visitable, context: Seq[Visitable])
      (lifted: (Visited[T], Visited[T]) => Visited[T])
      (func: (VisitOrder, Seq[Visitable]) => T): Visited[T] = {
    val newContext = Seq(self) ++ context

    val before: Visited[T] =
      try {
        Right(func(Before(self), context))
      } catch {
        case e: Throwable => Left(Seq(e))
      }

    val childResults = children.map { child =>
      child.visit(child, newContext)(lifted)(func)
    }

    val after: Visited[T] =
      try {
        Right(func(After(self), context))
      } catch {
        case e: Throwable => Left(Seq(e))
      }

    lifted((before /: childResults)(lifted), after)
  }
}

