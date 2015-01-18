package juicy.utils

import scala.reflect.ClassTag

package object visitor {

trait VisitOrder
case class Before(n: Visitable) extends VisitOrder
case class After(n: Visitable) extends VisitOrder

case class VisitError(errors: Seq[CompilerError]) extends Throwable

// Check the context to see if the current node is inside of a T
def isIn[T : ClassTag]
    (whenFound: T => Boolean = { _: T => true })
    (implicit context: Seq[Visitable]): Boolean = {
  // The JVM deletes our type-info, so this gets it back
  val clazz = implicitly[ClassTag[T]].runtimeClass
  (false /: context) { (last, node) =>
    last || (node match {
      case found if clazz.isInstance(found) => whenFound(found.asInstanceOf[T])
      case _                                => false
    })
  }
}

trait Visitable {
  import juicy.source.tokenizer._
  var originalToken: Token = new Token.Invalid()
  def from = originalToken.from
  def children: Seq[Visitable]

  type Visited[T] = Either[Seq[CompilerError], T]

  private def unwrapLeft[T](wrapped: Visited[T]): Seq[CompilerError] = {
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
        case e: CompilerError => Left(Seq(e))
      }

    val childResults = children.map { child =>
      child.visit(child, newContext)(lifted)(func)
    }

    val after: Visited[T] =
      try {
        Right(func(After(self), context))
      } catch {
        case e: CompilerError => Left(Seq(e))
      }

    lifted((before /: childResults)(lifted), after)
  }
}

}
