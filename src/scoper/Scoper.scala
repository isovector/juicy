package juicy.source.scoper

import juicy.source.ast._
import juicy.source.ast.Modifiers._
import juicy.source.tokenizer._
import juicy.utils.CompilerError
import juicy.utils.visitor._

case class ScopeError(msg: String, from: SourceLocation) extends CompilerError

object Hashtag360NoScoper {

  def check(which: Modifiers.Value, flag: Modifiers.Value) =
    (which & flag) == flag

  def apply(node: Visitable): ClassScope = {
    var curBlock: Option[BlockScope] = None
    var curClass: ClassScope = null

    def makeChildScope(from: SourceLocation) = {
      if (curBlock.isEmpty) {
        throw new ScopeError("Invalid scoping", from)
      } else {
        curBlock = Some(new BlockScope(curBlock))
      }
    }

    def freeChildScope(from: SourceLocation) = {
      if (curBlock.isEmpty) {
        throw new ScopeError("Somehow outside a non-existent scope", from)
      } else {
        curBlock = curBlock.get.parent
      }
    }

    node.visit { (self, context) =>
      implicit val ctx = context
      val from = self match {
        case Before(b) => b.from
        case After(a) => a.from
      }
      self match {
        case Before(c@ClassDefn(_,_,_,_,_,fields,methods,_)) => {
            if (!curBlock.isEmpty) {
                throw new ScopeError("Nested classes forbidden", from)
            } else {
                curClass = new ClassScope()
                curBlock = Some(curClass)
                fields.foreach(f => 
                  if(!curClass.define(f.name, f.tname)) {
                    val source = curClass.resolve(f.name).get.from
                    throw new ScopeError(s"Duplicate field $f.name (originally defined at: $source)", from)
                  }
                )
                methods.foreach(m => 
                  if (!curClass.defineMethod(m.name, m.params.map(_.tname)))
                 throw new ScopeError("Duplicate Method " + m.name + " with parameters " + m.params.mkString, from)
               )
            }
        }
        case Before(MethodDefn(name,_,_,_,fields,_)) => {
          makeChildScope(from)
        }
        case Before(VarStmnt(name,_,tname,_)) => {
          if(curBlock.isEmpty) {
               throw new ScopeError(s"Definition of $name outside class body", from)
           } else if (curBlock.get != curClass && !curBlock.get.define(name, tname)) {
             // Already defined
             val source = curBlock.get.resolve(name).get.from
             throw new ScopeError(s"Duplicate definition of variable $name (originally defined at: $source)", from)
           }
        }
        case Before(_: WhileStmnt) => {
          makeChildScope(from)
        }
        case After(_: WhileStmnt) => {
          freeChildScope(from)
        }
        case Before(_: ForStmnt) => {
          makeChildScope(from)
        }
        case After(_:ForStmnt) => {
          freeChildScope(from)
        }
        case Before(_: BlockStmnt) => {
          makeChildScope(from)
        }
        case After(_: BlockStmnt) => {
          freeChildScope(from)
        }
        case After(_: ClassDefn) => {
          freeChildScope(from)
        }
        case After(_: MethodDefn) => {
          freeChildScope(from)
        }
        case _ =>
      }
      self match {
        case Before(v) => {
          if (curBlock.isDefined) {
            v.scope = curBlock.get
          }
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
