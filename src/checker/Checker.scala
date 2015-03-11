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
    /*
    var scopeMap = Map[Visitable, ClassScope]()
    
    def addSubScope(v: Visitable, tn: Typename) = {
      scopeMap += (v -> tn.resolved.flatMap(_.scope)
                          .getOrElse(new ClassScope())
                          .enclosingClass)
    }
    
    scopeMap += (ThisVal() -> node.classScope)
    */
    node.rewrite(Rewriter {(self, context) => 
      implicit val ctx = context
      self match {
        case i: Id =>
          val isVariable = context.head match {
            case Member(_, r) if r == i => false
            case Callee(f) if f == i => false
            case v: VarStmnt => false
            case _ => true
          }
          if (isVariable) {
            val name = i.name
            val tn = i.scope.get.resolve(name)
            if (tn.isEmpty) {
              errors :+= undefined(i)
            } else {
              i.exprType = tn
            }
          }
          i
        case m@Member(left, right) =>
          val scope = left.typeScope
          if (scope.isDefined) {
            if (context.head != Callee(m)) {
                right match {
                  case ri: Id =>
                    val rname = ri.name
                    val tn = scope.map(_.enclosingClass).flatMap(_.resolve(rname))
                    
                    if (tn.isEmpty) {
                      errors :+= undefined(ri)
                    } else {
                      m.exprType = tn
                    }
                }
            }
          }
          m
        case c@Call(method, fields) =>
          val (scope, ident) = method.expr match {
            case id: Id => (Some(node.classScope), id.name)
            case Member(left, right) => (left.typeScope, right.asInstanceOf[Id].name)
            case e: Expression => throw new CheckerError(s"How the fuck did $e you get here?", e.from)
          }
          if (scope.isDefined) {
            if(fields.filter(!_.hasType).isEmpty) {
              val tn = scope.get.resolveMethod(ident, fields.map(_.exprType.get))
              if (tn.isEmpty) {
                errors :+= undefined(c)
              } else {
                c.exprType = tn
              }
            }
          }
          c
        /*
        case i: IntVal =>
          i.exprType = pkgTree.getType(Seq("int"))
          i
        case t: TrueVal =>
          t.exprType = pkgTree.getType(Seq("boolean"))
          t
        case f: FalseVal =>
          f.exprType = pkgTree.getType(Seq("boolean"))
          f
        case c: CharLiteral =>
          c.exprType = pkgTree.getType(Seq("char"))
          c
        case s: StringLiteral =>
          s.exprType = pkgTree.getType(Seq("java", "lang", "string"))
          s */
        case _ => self
      }
    })
    if (!errors.isEmpty) {
      throw new VisitError(errors)
    }
  }
}
