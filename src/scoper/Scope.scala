package juicy.source.scoper
import juicy.source.ast.Signature
import juicy.source.ast.Typename
import juicy.source.tokenizer.SourceLocation

abstract class Scope {
  val parent: Scope

  val variables = collection.mutable.Map[String, Typename]()
  var orderedDecls = Seq[String]()
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
      orderedDecls :+= varname
      true
    }
  }

  def definedBefore(before: String, after: String): Boolean = {
    val isSameVar = before == after
    val acrossScope = !isActuallyLocalScope(before) && isActuallyLocalScope(after)

    if (isSameVar)
      false
    else if (acrossScope)
      true
    else
      (orderedDecls.indexOf(before) < orderedDecls.indexOf(after)) ||
        parent.resolve(before).isDefined
  }

  def enclosingClass(): ClassScope

  def printVariables(indent: Int = 0): Unit = {
    variables.foreach {kv => println(" " * indent + kv) }
    printParent(indent + 1)
  }

  def printParent(id: Int) = parent.printVariables(id)
  def isLocalScope(id: String): Boolean = {
    isActuallyLocalScope(id) || parent.isLocalScope(id)
  }

  def isActuallyLocalScope(id: String): Boolean = {
    variables contains id
  }

  def localVarStackIndex(id: String): Int = {
    val i = orderedDecls.indexOf(id)
    if (i < 0) parent.localVarStackIndex(id)
    else i + parent.stackSize
  }

  def maxStackIndex: Int = {
    var determinants = children.map(_.maxStackIndex)
    if (orderedDecls.length > 0)
      determinants :+= localVarStackIndex(orderedDecls.last) + 1

    if (determinants.length > 0)
      determinants.max
    else 0
  }

  def stackSize: Int
}

class BlockScope (val parent: Scope) extends Scope {
  parent.children :+= this
  def enclosingClass() = parent.enclosingClass

  override def stackSize: Int = {
    orderedDecls.length + parent.stackSize
  }
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
  override def isLocalScope(id: String) = false

  override def stackSize: Int = 0
}
