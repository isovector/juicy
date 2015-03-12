package juicy.source.scoper
import juicy.source.ast.Signature
import juicy.source.ast.Typename
import juicy.source.tokenizer.SourceLocation

abstract class Scope {
  val parent: Scope
  
  val variables = collection.mutable.Map[String, Typename]()
  var children = Seq[Scope]()
  
  def resolve(varname: String) : Option[Typename] = {
    if (variables contains varname) {
      return Some(variables(varname))
    } else {
      parent.resolve(varname)
    }
  }
  
  def resolveParent(varname: String): Boolean = {
    if (variables contains varname) {
      true
    } else {
      parent.resolveParent(varname)
    }
  }
  
  def define(varname: String, tname: Typename): Boolean = {
    if (parent.resolveParent(varname)) {
      false
    } else if (variables contains varname) {
      false
    } else {
      variables(varname) = tname
      true
    }
  }
  def enclosingClass(): ClassScope
  
  def printVariables(indent: Int = 0): Unit = {
    variables.foreach {kv => println(" " * indent + kv) }
    printParent(indent + 1)
  }
  
  def printParent(id: Int) = parent.printVariables(id)
}

class BlockScope (val parent: Scope) extends Scope {
  parent.children :+= this
  def enclosingClass() = parent.enclosingClass
}

class ClassScope extends Scope {
  val parent = this
  
  val methods = collection.mutable.Map[Signature, Typename]()
  def defineMethod(name: String, fields: Seq[Typename], ret: Typename): Boolean = {
    val signature = Signature(name, fields)
    if (methods contains signature) {
        false
    } else {
        methods(signature) = ret
        true
    }
  }
  override def resolve(varname: String) = variables.get(varname)
  override def resolveParent(varname: String) = false
  def resolveMethod(name: String, fields: Seq[Typename]) = methods.get(Signature(name, fields))
  
  override def enclosingClass() = this
  override def printParent(indent: Int) = {}
}