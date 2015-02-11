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

case class MethodSignature (name: String, fields: Seq[Typename])

class ClassScope extends BlockScope {
  val methods = collection.mutable.Set[MethodSignature]()
  def defineMethod(name: String, fields: Seq[Typename]): Boolean = {
    val signature = MethodSignature(name, fields)
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

  def apply(node: Visitable): Either[Seq[CompilerError],Boolean] = {
    var curBlock: Option[BlockScope] = None
    var curClass: ClassScope = null
    
    def makeChildScope(): Boolean = {
      if (curBlock.isEmpty) {
        false
      } else {
        curBlock = Some(new BlockScope(curBlock))
        true
      }
    }
    
    def freeChildScope() = {
      if (curBlock.isEmpty) {
        false
      } else {
        curBlock = curBlock.get.parent
        true
      }
    }
    
    node.visit((a: Boolean, b: Boolean) => a && b)
    { (self, context) =>
      self match {
        case Before(c@ClassDefn(_,_,_,_,fields,_,_,_)) => {
            if (!curBlock.isEmpty) {
                throw new ScopeError("Nested classes forbidden", c.from)
            } else {
                curClass = new ClassScope()
                curBlock = Some(curClass)
                fields.foreach(f => curClass.define(f.name, f.tname))
                true
            }
        }
        case Before(m@MethodDefn(name,_,_,fields,_)) => {
          if (!makeChildScope()) {
            false
          } else if (!curClass.defineMethod(name, fields.map(_.tname))){
            throw new ScopeError(s"Duplicate definition of method $name with parameters $fields", m.from)
          } else {            
            fields.foreach(f => curBlock.get.define(f.name, f.tname))
            true
          }
        }
        case Before(v@VarStmnt(name,_,tname,_)) => {
           if(curBlock.isEmpty) {
               throw new ScopeError(s"Definition of $name outside class body", v.from)
           } else if (curBlock.get == curClass) {
             // Ignore field decls if we get them again
             true
           } else if (!curBlock.get.define(name, tname)) {
             // Already defined
             throw new ScopeError(s"Duplicate definition of variable $name", v.from)
           } else {
             true
           }
        }
        case Before(WhileStmnt(_,_)) => {
          makeChildScope()
        }
        case After(WhileStmnt(_,_)) => {
          freeChildScope()
        }
        case Before(i@Id(name)) => {
          if (curBlock.isEmpty) {
            throw new ScopeError("Variable $name used outside class scope", i.from)
          } else if (curBlock.get.resolve(name).isDefined) {
            true
          } else {
            throw new ScopeError("Undefined reference to $name", i.from)
          }
        }
        case Before(ForStmnt(_,_,_,_)) => {
          makeChildScope()
        }
        case After(ForStmnt(_,_,_,_)) => {
          freeChildScope()
        }
        case Before(BlockStmnt(_)) => {
          makeChildScope()
        }
        case After(BlockStmnt(_)) => {
          freeChildScope()
        }
        case After(ClassDefn(_,_,_,_,_,_,_,_)) => {
          if(!freeChildScope()) {
            false
          } else {
            curClass = null
            true
          }
        }
        case After(MethodDefn(_,_,_,_,_)) => {
          freeChildScope()
        }
      }
    }
  }
}
