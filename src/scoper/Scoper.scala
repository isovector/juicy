package juicy.source.scoper

import juicy.source.ast._
import juicy.source.tokenizer._
import juicy.source.ast.Modifiers._
import juicy.utils.visitor._
import juicy.utils.CompilerError

case class ScopeError(msg: String, from: SourceLocation) extends CompilerError

class BlockScope(val parent: Option[BlockScope] = None){
  var children = List[BlockScope]()
  if (parent.isDefined) {
    parent.get.children ::= this
  }
  val variables = collection.mutable.Map[String, Typename]()
  def resolve(varname: String) : Option[Typename] = {
    if (variables contains varname) {
      return Some(variables(varname))
    } else if (parent.isEmpty) {
       None
    } else {
      parent.get.resolve(varname)
    }
  }
  def define(varname: String, tname: Typename): Boolean = {
    if (parent.isDefined && parent.get.resolveParent(varname)) {
      false
    } else if (resolveChildren(varname)) {
      false
    } else {
      variables(varname) = tname
      true
    }
  }
  def resolveParent(varname: String): Boolean = {
    if (variables contains varname) {
      true
    } else if (parent.isDefined){
      parent.get.resolveParent(varname)
    } else {
      false
    }
  }
  def resolveChildren(varname: String): Boolean = {
    if (children.find(_.variables contains varname) != None) {
      true
    } else if (children.find(_.resolveChildren(varname)) != None) {
      true
    } else {
      false
    }
  }
}

case class MethodSignature (name: String, fields: Seq[Typename], isCxr: Boolean)

class ClassScope extends BlockScope {
  val methods = collection.mutable.Set[MethodSignature]()
  def defineMethod(name: String, fields: Seq[Typename], isCxr: Boolean): Boolean = {
    val signature = MethodSignature(name, fields, isCxr)
    if (methods contains signature) {
        false
    } else {
        methods.add(signature)
        true
    }
  }
  override def resolveChildren(varname: String) = false
  override def resolveParent(varname: String) = false
}

object Hashtag360NoScoper {
    
  def check(which: Modifiers.Value, flag: Modifiers.Value) =
    (which & flag) == flag

  def apply(node: Visitable): Boolean = {
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
    
    node.visit((a: Unit, b: Unit) => {})
    { (self, context) =>
      val from = self match {
        case Before(b) => b.from
        case After(a) => a.from
      }
      self match {
        case Before(c@ClassDefn(_,_,_,_,fields,_,methods,_)) => {
            if (!curBlock.isEmpty) {
                throw new ScopeError("Nested classes forbidden", from)
            } else {
                curClass = new ClassScope()
                curBlock = Some(curClass)
                fields.foreach(f => curClass.define(f.name, f.tname))
                methods.foreach(m => curClass.defineMethod(m.name, m.params.map(_.tname), m.isConstructor))
            }
        }
        case Before(MethodDefn(name,_,_,fields,_)) => {
          makeChildScope(from)
          fields.foreach(f => curBlock.get.define(f.name, f.tname))
        }
        case Before(VarStmnt(name,_,tname,_)) => {
          if(curBlock.isEmpty) {
               throw new ScopeError(s"Definition of $name outside class body", from)
           } else if (curBlock.get != curClass && !curBlock.get.define(name, tname)) {
             // Already defined
             throw new ScopeError(s"Duplicate definition of variable $name", from)
           }
        }
        case Before(WhileStmnt(_,_)) => {
          makeChildScope(from)
        }
        case After(WhileStmnt(_,_)) => {
          freeChildScope(from)
        }
        case Before(Id(name)) => {
          if (curBlock.isEmpty) {
            throw new ScopeError("Variable $name used outside class scope", from)
          } else if (!curBlock.get.resolve(name).isDefined) {
            throw new ScopeError(s"Undefined reference to $name", from)
          }
        }
        case Before(ForStmnt(_,_,_,_)) => {
          makeChildScope(from)
        }
        case After(ForStmnt(_,_,_,_)) => {
          freeChildScope(from)
        }
        case Before(BlockStmnt(_)) => {
          makeChildScope(from)
        }
        case After(BlockStmnt(_)) => {
          freeChildScope(from)
        }
        case After(ClassDefn(_,_,_,_,_,_,_,_)) => {
          freeChildScope(from)
          curClass = null
        }
        case After(MethodDefn(_,_,_,_,_)) => {
          freeChildScope(from)
        }
        case _ => 
      }
    }.fold(
      l => {
        throw new VisitError(l)
      },
      r => true
    )
  }
}
