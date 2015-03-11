package juicy.source.scoper

import juicy.source.ast._
import juicy.source.ast.Modifiers._
import juicy.source.tokenizer._
import juicy.utils.CompilerError
import juicy.utils.visitor._

case class ScopeError(msg: String, from: SourceLocation) extends CompilerError

object Hashtag360NoScoper {

  def apply(node: Visitable): ClassScope = {
    val curClass = new ClassScope()
    var curBlock: Scope = curClass

    def makeChildScope() = {
      curBlock = new BlockScope(curBlock)
    }

    def freeChildScope() = {
      curBlock = curBlock.parent
    }

    node.visit { (self, context) =>
      implicit val ctx = context
      val from = self match {
        case Before(b) => b.from
        case After(a) => a.from
      }
      self match {
        case Before(c@ClassDefn(_,_,_,_,_,fields,methods,_)) => {
            fields.foreach(f => 
              if(!curClass.define(f.name, f.tname)) {
                val source = curClass.resolve(f.name).get.from
                throw new ScopeError(s"Duplicate field $f.name (originally defined at: $source)", from)
              }
            )
            methods.foreach(m => 
              if (!curClass.defineMethod(m.name, m.params.map(_.tname), m.tname))
             throw new ScopeError("Duplicate Method " + m.name + " with parameters " + m.params.mkString, from)
           )
        }
        case Before(MethodDefn(name,_,_,_,fields,_)) => {
          makeChildScope()
        }
        case Before(VarStmnt(name,_,tname,_)) => {
          if (curBlock != curClass && !curBlock.define(name, tname)) {
             // Already defined
             val source = curBlock.resolve(name).get.from
             throw new ScopeError(s"Duplicate definition of variable $name (originally defined at: $source)", from)
           }
        }
        case Before(_: WhileStmnt) => {
          makeChildScope()
        }
        case After(_: WhileStmnt) => {
          freeChildScope()
        }
        case Before(_: ForStmnt) => {
          makeChildScope()
        }
        case After(_:ForStmnt) => {
          freeChildScope()
        }
        case Before(_: BlockStmnt) => {
          makeChildScope()
        }
        case After(_: BlockStmnt) => {
          freeChildScope()
        }
        case After(_: ClassDefn) => {
          freeChildScope()
        }
        case After(_: MethodDefn) => {
          freeChildScope()
        }
        case _ =>
      }
      self match {
        case Before(v) => {
          v.scope = Some(curBlock)
        }
        case _ =>
      }
    }.fold(
      l => {
        throw new VisitError(l)
      },
      r => curClass
    )
  }
}
