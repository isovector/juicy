package juicy.source.scoper
import juicy.source.ast.Signature
import juicy.source.ast.Typename
import juicy.source.tokenizer.SourceLocation

class BlockScope (val parent: Option[BlockScope]=None) {
  var children = List[BlockScope]()
  if (parent.isDefined) {
    parent.get.children ::= this
  }
  val variables = collection.mutable.Map[String, (Typename, SourceLocation)]()
  def resolve(varname: String) : Option[(Typename, SourceLocation)] = {
    if (variables contains varname) {
      return Some(variables(varname))
    } else if (parent.isEmpty) {
       None
    } else {
      parent.get.resolve(varname)
    }
  }
  def define(varname: String, tname: Typename, source: SourceLocation): Boolean = {
    if (parent.isDefined && parent.get.resolveParent(varname)) {
      false
    } else if (variables contains varname) {
      false
    } else {
      variables(varname) = (tname, source)
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
}

class ClassScope extends BlockScope {
  val methods = collection.mutable.Set[Signature]()
  def defineMethod(name: String, fields: Seq[Typename]): Boolean = {
    val signature = Signature(name, fields)
    if (methods contains signature) {
        false
    } else {
        methods.add(signature)
        true
    }
  }
  override def resolveParent(varname: String) = false
  def resolveMethod(name: String) = {
    methods.filter(_.name == name)
  }
}