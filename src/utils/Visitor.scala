package juicy.utils

import juicy.source.scoper.Scope
import scala.reflect.ClassTag

package object visitor {

case class VisitError(errors: Seq[CompilerError]) extends Exception {
  override def toString(): String = errors.map(_.toString).mkString("\n")
}

trait VisitOrder
case class Before(n: Visitable) extends VisitOrder
case class After(n: Visitable) extends VisitOrder
case class EmptyNode() extends Visitable {
  val children = Seq()
  def rewrite(rule: Rewriter, context: Seq[Visitable]) = this
}

def before(order: VisitOrder): Visitable =
  order match {
    case Before(n) => n
    case After(n) => EmptyNode()
  }

def after(order: VisitOrder): Visitable =
  order match {
    case Before(n) => EmptyNode()
    case After(n) => n
  }

type Visited[T] = Either[Seq[CompilerError], T]

object Visited {
  def unwrapLeft[T](wrapped: Visited[T]): Seq[CompilerError] = {
    wrapped match {
      case Left(unwrapped) => unwrapped
      case _               => Seq()
    }
  }

  def unwrapRight[T](wrapped: Visited[T]): T = {
    wrapped match {
      case Right(unwrapped) => unwrapped
      case _                => throw new Exception("impossibruuuu")
    }
  }
}

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

def ancestor[T : ClassTag](implicit context: Seq[Visitable]): Option[T] = {
  val clazz = implicitly[ClassTag[T]].runtimeClass
  context.foreach { node =>
    if (clazz.isInstance(node))
      return Some(node.asInstanceOf[T])
  }

  None
}

trait Visitable {
  import juicy.source.tokenizer._
  var originalToken: Token = new Token.Invalid()

  var scope: Option[Scope] = None
  def classScope = scope.get.enclosingClass

  def from = originalToken.from
  val children: Seq[Visitable]

  def visit(func: (VisitOrder, Seq[Visitable]) => Unit): Visited[Unit] = {
    def lifted(a: Visited[Unit], b: Visited[Unit]): Visited[Unit] =
      if (a.isLeft || b.isLeft)
        Left(Visited.unwrapLeft(a) ++ Visited.unwrapLeft(b))
      else
        Right({ })

    visit(Seq())(lifted)(func)
  }

  private def visit
      (context: Seq[Visitable])
      (lifted: (Visited[Unit], Visited[Unit]) => Visited[Unit])
      (func: (VisitOrder, Seq[Visitable]) => Unit): Visited[Unit] = {
    val newContext = Seq(this) ++ context

    val before: Visited[Unit] =
      try {
        Right(func(Before(this), context))
      } catch {
        case e: CompilerError => Left(Seq(e))
      }

    val childResults = children.map { child =>
      child.visit(newContext)(lifted)(func)
    }

    val after: Visited[Unit] =
      try {
        Right(func(After(this), context))
      } catch {
        case e: CompilerError => Left(Seq(e))
      }

    lifted((before /: childResults)(lifted), after)
  }

  def rewrite(rule: Rewriter): Visitable = rewrite(rule, Seq())
  def rewrite(rule: Rewriter, context: Seq[Visitable]): Visitable

  def transfer(dst: Visitable): Visitable = {
    dst.scope         = scope
    dst.originalToken = originalToken
    dst
  }
}

case class Rewriter(rule: (Visitable, Seq[Visitable]) => Visitable) {
  def apply(node: Visitable, context: Seq[Visitable]): Visitable = {
    val result = rule(node, context)
    result.originalToken = node.originalToken
    result.scope = node.scope
    result
  }
}

}
