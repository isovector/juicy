package juicy.source.checker

import juicy.source.ast._
import juicy.source._
import juicy.utils.visitor._
import juicy.source.tokenizer._
import juicy.utils._
import juicy.source.scoper._

case class CheckerError (msg: String, from: SourceLocation) extends CompilerError


object Checker {
  def undefined (v: Expression) = CheckerError(s"Undefined symbol $v", v.from)

  def apply(node: Visitable, pkgTree: PackageTree): Unit = {
    var errors = Seq[CompilerError]()
    var scopeMap = Map[Visitable, ClassScope]()
    
    def addSubScope(v: Visitable, tn: Typename) = {
      scopeMap += (v -> tn.resolved.map(_.scope).flatMap(_.enclosingClass).get)
    }
    
    scopeMap += (ThisVal() -> node.classScope.get)
    
    node.rewrite(Rewriter {(self, context) => 
      implicit val ctx = context
      self match {
        case i: Id =>
          if (!isIn[Member]()) {
            val name = i.name
            val tn = i.scope.resolve(name)
            if (tn.isEmpty) {
              errors :+= undefined(i)
            } else {
              i.exprType = tn
              addSubScope(i, tn.get)
            }
          }
          i
        case m@Member(left, right) =>
          val scope = scopeMap.get(left)
          if (scope.isDefined) {
            if (context.head == Callee(m)) {
        
            } else {
                right match {
                  case ri: Id =>
                    val rname = ri.name
                    val tn = scope.get.enclosingClass.get.resolve(ri.name)
                    if (tn.isEmpty) {
                      errors :+= undefined(ri)
                    } else {
                      ri.exprType = tn
                      addSubScope(m, tn.get)
                    }
                }
            }
          }
          m
        case _ => self
      }
    })
    if (!errors.isEmpty) {
      throw new VisitError(errors)
    }
  }
}