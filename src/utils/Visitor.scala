package juicy.utils.visitor

trait VisitOrder
case class Before(n: Visitable) extends VisitOrder
case class After(n: Visitable) extends VisitOrder

trait Visitable {
  import juicy.source.tokenizer._
  var originalToken: Token = new Token.Invalid()

  def children: Seq[Visitable]

  def visit[T]
      (fold: (T, T) => T)
      (func: (VisitOrder, Seq[Visitable]) => T): T = {
    visit(this, Seq())(fold)(func)
  }

  def visit[T]
      (self: Visitable, context: Seq[Visitable])
      (fold: (T, T) => T)
      (func: (VisitOrder, Seq[Visitable]) => T): T = {
    val newContext = Seq(self) ++ context
    fold(
      (func(Before(self), context) /:
        children.map( child =>
          child.visit(child, newContext)(fold)(func)
        )
      )(fold), func(After(self), context))
  }
}

