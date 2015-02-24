package juicy.source.scoper

import juicy.source.ast._
import juicy.source.ast.Modifiers._
import juicy.source.tokenizer._
import juicy.utils.CompilerError
import juicy.utils.visitor._

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
  def resolveMethod(name: String) = {
    methods.filter(_.name == name)
  }
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

    node.visit { (self, context) =>
      implicit val ctx = context
      val from = self match {
        case Before(b) => b.from
        case After(a) => a.from
      }
      self match {
        case Before(c@ClassDefn(_,_,_,_,fields,methods,_)) => {
            if (!curBlock.isEmpty) {
                throw new ScopeError("Nested classes forbidden", from)
            } else {
                curClass = new ClassScope()
                curBlock = Some(curClass)
                fields.foreach(f => curClass.define(f.name, f.tname))
                methods.foreach(m => curClass.defineMethod(m.name, m.params.map(_.tname), m.isCxr))
            }
        }
        case Before(MethodDefn(name,_,_,_,fields,_)) => {
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
          curClass = null
        }
        case After(_: MethodDefn) => {
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
