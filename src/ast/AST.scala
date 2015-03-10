package juicy.source.ast

import juicy.source.ambiguous.AmbiguousStatus
import juicy.utils.Implicits._
import juicy.utils.visitor._

object Modifiers {
  type Value = Int

  // Update parse() if you change this list
  val NONE      = 0
  val PUBLIC    = 1 << 0
  val PROTECTED = 1 << 1
  val STATIC    = 1 << 2
  val EXTERN    = 1 << 3
  val NATIVE    = 1 << 4
  val ABSTRACT  = 1 << 5
  val FINAL     = 1 << 6

  def parse(str: String): Value = {
    str.toLowerCase match {
      case "public"    => PUBLIC
      case "protected" => PROTECTED
      case "static"    => STATIC
      case "extern"    => EXTERN
      case "native"    => NATIVE
      case "abstract"  => ABSTRACT
      case "final"     => FINAL
    }
  }
}

trait Expression extends Visitable {
  var exprType: Option[Typename] = None
  def hasType = exprType.isDefined
}
trait Statement extends Visitable
trait Definition extends Visitable
trait ImportStmnt extends Statement

trait BinOp extends Expression {
  val lhs: Expression
  val rhs: Expression

  protected def rewriter[T <: BinOp]
      (ctor: (Expression, Expression) => T)
      (rule: Rewriter, context: Seq[Visitable]) = {
    val newContext = this +: context
    rule(
      ctor(
        lhs.rewrite(rule, newContext).asInstanceOf[Expression],
        rhs.rewrite(rule, newContext).asInstanceOf[Expression]
      ), context)
  }

  val children = Seq(lhs, rhs)
}

trait UnOp extends Expression {
  val ghs: Expression

  protected def rewriter[T <: UnOp]
      (ctor: Expression => T)
      (rule: Rewriter, context: Seq[Visitable]) = {
    val newContext = this +: context
    rule(
      ctor(
        ghs.rewrite(rule, newContext).asInstanceOf[Expression]
      ), context)
  }

  val children = Seq(ghs)
}

case class Typename(qname: QName, isArray: Boolean=false) extends Visitable {
  var resolved: Option[ClassDefn] = None
  val name = qname.mkString(".")
  val brackets = if (isArray) " []" else ""
  val isPrimitive =
    Seq("void", "boolean", "int", "short", "byte", "char").contains(name)
  override def toString() = s"$name$brackets"

  override def equals(o: Any) = o match {
    case that: Typename =>
      if (resolved.isDefined)
        resolved == that.resolved
      else
         ( qname == that.qname
        && isArray == that.isArray
         )

    case _              => false
  }

  override def hashCode = resolved match {
    case Some(classDef) => classDef.hashCode
    case _              => qname.hashCode + isArray.hashCode
  }

  val children = Seq()

  def rewrite(rule: Rewriter, context: Seq[Visitable]) = this
}

case class FileNode(
  pkg: QName,
  imports: Seq[ImportStmnt],
  classes: Seq[ClassDefn]
) extends Visitable {
  val children = imports ++ classes
  var typeScope: Option[Map[String, Seq[SuburbanClassDefn]]] = None

  def rewrite(rule: Rewriter, context: Seq[Visitable]) = {
    val newContext = this +: context
    rule(
      FileNode(
        pkg,
        imports.map(_.rewrite(rule, newContext).asInstanceOf[ImportStmnt]),
        classes.map(_.rewrite(rule, newContext).asInstanceOf[ClassDefn])
      ), context)
  }
}

object ClassDefn {
  private var autoIncr = 0
  private var enableIncr = true

  def getNextEqualityComparitor: Int = {
    if (enableIncr)
      autoIncr += 1
    autoIncr
  }

  def suspendUniqueness[T](c: => T) = {
    enableIncr = false
    c
    enableIncr = true
  }
}

case class ClassDefn(
  name: String,
  pkg: QName,
  mods: Modifiers.Value,
  extnds: Seq[Typename],
  impls: Seq[Typename],
  fields: Seq[VarStmnt],
  methods: Seq[MethodDefn],
  isInterface: Boolean = false
) extends Definition {
  val equalityComparitor = ClassDefn.getNextEqualityComparitor

  val children = extnds ++ impls ++ fields ++ methods

  val isClass = !isInterface

  lazy val (allMethods: Seq[MethodDefn], hidesMethods: Seq[MethodDefn]) = {
    val parentMethods =
      extnds.flatMap(_.resolved.get.allMethods).filter(!_.isCxr)
    val sigs = methods.map(_.signature)
    val (hides, keeps) = parentMethods.partition { parMeth =>
      sigs.contains(parMeth.signature)
    }

    (methods ++ keeps, hides)
  }

  lazy val allInterfaces: Seq[ClassDefn] = {
    val resolvedExtnds = extnds.map(_.resolved.get)
    val resolvedImpls = impls.map(_.resolved.get)

     ( resolvedExtnds
    ++ resolvedImpls
    ++ resolvedExtnds.flatMap(_.allInterfaces)
    ++ resolvedImpls.flatMap(_.allInterfaces)
     )
      .filter(_.isInterface)
  }

  override def equals(o: Any) = o match {
    case that: ClassDefn =>
       ( name               == that.name
      && pkg                == that.pkg
      && mods               == that.mods
      && extnds             == that.extnds
      && impls              == that.impls
      && fields             == that.fields
      && methods            == that.methods
      && isInterface        == that.isInterface
      && equalityComparitor == that.equalityComparitor
       )
    case _               => false
  }

  override def hashCode = name.hashCode + equalityComparitor.hashCode

  def rewrite(rule: Rewriter, context: Seq[Visitable]) = {
    val newContext = this +: context
    rule(
      ClassDefn(
        name,
        pkg,
        mods,
        extnds.map(_.rewrite(rule, newContext).asInstanceOf[Typename]),
        impls.map(_.rewrite(rule, newContext).asInstanceOf[Typename]),
        fields.map(_.rewrite(rule, newContext).asInstanceOf[VarStmnt]),
        methods.map(_.rewrite(rule, newContext).asInstanceOf[MethodDefn]),
        isInterface
      ), context)
  }

  def resolvesTo (other: ClassDefn) =
    name == other.name && pkg == other.pkg
}

case class ImportClass(
  tname: Typename
) extends ImportStmnt {
  val children = Seq(tname)

  def rewrite(rule: Rewriter, context: Seq[Visitable]) = {
    val newContext = this +: context
    rule(
      ImportClass(
        tname.rewrite(rule, newContext).asInstanceOf[Typename]
      ), context)
  }
}

case class ImportPkg(
  pkg: QName
) extends ImportStmnt {
  val children = Seq()

  def rewrite(rule: Rewriter, context: Seq[Visitable]) = this
}

case class Signature(name: String, params: Seq[Typename])
case class MethodDefn(
  name: String,
  mods: Modifiers.Value,
  isCxr: Boolean,
  tname: Typename,
  params: Seq[VarStmnt],
  body: Option[Statement]
) extends Definition {
  val children = Seq(tname) ++ params ++ body.toList

  val signature = Signature(name, params.map(_.tname))

  // Equivalency equality
  def ~==(other: MethodDefn): Boolean =
    signature == other.signature

  def rewrite(rule: Rewriter, context: Seq[Visitable]) = {
    val newContext = this +: context
    rule(
      MethodDefn(
        name,
        mods,
        isCxr,
        tname.rewrite(rule, newContext).asInstanceOf[Typename],
        params.map(_.rewrite(rule, newContext).asInstanceOf[VarStmnt]),
        body.map(_.rewrite(rule, newContext).asInstanceOf[Statement])
      ), context)
  }
}

case class VarStmnt(
  name: String,
  mods: Modifiers.Value,
  tname: Typename,
  value: Option[Expression]
) extends Statement {
  val children = tname +: value.toList

  def rewrite(rule: Rewriter, context: Seq[Visitable]) = {
    val newContext = this +: context
    rule(
      VarStmnt(
        name,
        mods,
        tname.rewrite(rule, newContext).asInstanceOf[Typename],
        value.map(_.rewrite(rule, newContext).asInstanceOf[Expression])
      ), context)
  }
}

case class IfStmnt(
  cond: Expression,
  then: BlockStmnt,
  otherwise: Option[BlockStmnt]
) extends Statement {
  val children = Seq(cond, then) ++ otherwise.toList

  def rewrite(rule: Rewriter, context: Seq[Visitable]) = {
    val newContext = this +: context
    rule(
      IfStmnt(
        cond.rewrite(rule, newContext).asInstanceOf[Expression],
        then.rewrite(rule, newContext).asInstanceOf[BlockStmnt],
        otherwise.map(_.rewrite(rule, newContext).asInstanceOf[BlockStmnt])
      ), context)
  }
}

case class WhileStmnt(
  cond: Expression,
  body: Statement
) extends Statement {
  val children = Seq(cond, body)

  def rewrite(rule: Rewriter, context: Seq[Visitable]) = {
    val newContext = this +: context
    rule(
      WhileStmnt(
        cond.rewrite(rule, newContext).asInstanceOf[Expression],
        body.rewrite(rule, newContext).asInstanceOf[Statement]
      ), context)
  }
}

case class ForStmnt(
  first: Option[Statement],
  cond: Option[Expression],
  after: Option[Expression],
  body: Statement
) extends Statement {
  val children = first.toList ++ cond.toList ++ after.toList ++ Seq(body)

  def rewrite(rule: Rewriter, context: Seq[Visitable]) = {
    val newContext = this +: context
    rule(
      ForStmnt(
        first.map(_.rewrite(rule, newContext).asInstanceOf[Statement]),
        cond.map(_.rewrite(rule, newContext).asInstanceOf[Expression]),
        after.map(_.rewrite(rule, newContext).asInstanceOf[Expression]),
        body.rewrite(rule, newContext).asInstanceOf[Statement]
      ), context)
  }
}

case class BlockStmnt(
  body: Seq[Statement]
) extends Statement {
  val children = body

  def rewrite(rule: Rewriter, context: Seq[Visitable]) = {
    val newContext = this +: context
    rule(
      BlockStmnt(
        body.map(_.rewrite(rule, newContext).asInstanceOf[Statement])
      ), context)
  }
}

case class ReturnStmnt(
  value: Expression
) extends Statement {
  val children = Seq(value)

  def rewrite(rule: Rewriter, context: Seq[Visitable]) = {
    val newContext = this +: context
    rule(
      ReturnStmnt(
        value.rewrite(rule, newContext).asInstanceOf[Expression]
      ), context)
  }
}

case class ExprStmnt(
  expr: Expression
) extends Statement {
  val children = Seq(expr)

  def rewrite(rule: Rewriter, context: Seq[Visitable]) = {
    val newContext = this +: context
    rule(
      ExprStmnt(
        expr.rewrite(rule, newContext).asInstanceOf[Expression]
      ), context)
  }
}

trait NullOp extends Expression {
  val children = Seq()

  def rewrite(rule: Rewriter, context: Seq[Visitable]) = rule(this, context)
}

case class NullVal()                extends NullOp
case class ThisVal()                extends NullOp
case class SuperVal()               extends NullOp
case class IntVal(value: Int)       extends NullOp
case class CharVal(value: Char)     extends NullOp
case class BoolVal(value: Boolean)  extends NullOp
case class StringVal(value: String) extends NullOp

case class Id(name: String)         extends NullOp {
  var status: AmbiguousStatus.Value = AmbiguousStatus.AMBIGUOUS
}

case class Callee (expr: Expression) extends Expression {
  val children = Seq(expr)
  def rewrite(rule: Rewriter, context: Seq[Visitable]) = {
    val newContext = this +: context
    Callee(expr.rewrite(rule, newContext).asInstanceOf[Expression])
  }
}

case class Call(
  method: Callee,
  args: Seq[Expression]
) extends Expression {
  val children = args :+ method

  def rewrite(rule: Rewriter, context: Seq[Visitable]) = {
    val newContext = this +: context
    rule(
      Call(
        method.rewrite(rule, newContext).asInstanceOf[Callee],
        args.map(_.rewrite(rule, newContext).asInstanceOf[Expression])
      ), context)
  }
}

case class Cast(
  tname: Typename,
  value: Expression
) extends Expression {
  val children = Seq(tname, value)

  def rewrite(rule: Rewriter, context: Seq[Visitable]) = {
    val newContext = this +: context
    rule(
      Cast(
        tname.rewrite(rule, newContext).asInstanceOf[Typename],
        value.rewrite(rule, newContext).asInstanceOf[Expression]
      ), context)
  }
}

case class NewType(
  tname: Typename,
  args: Seq[Expression]
) extends Expression {
  val children = tname +: args

  def rewrite(rule: Rewriter, context: Seq[Visitable]) = {
    val newContext = this +: context
    rule(
      NewType(
        tname.rewrite(rule, newContext).asInstanceOf[Typename],
        args.map(_.rewrite(rule, newContext).asInstanceOf[Expression])
      ), context)
  }
}

case class NewArray(
  tname: Typename,
  size: Expression
) extends Expression {
  val children = Seq(tname, size)

  def rewrite(rule: Rewriter, context: Seq[Visitable]) = {
    val newContext = this +: context
    rule(
      NewArray(
        tname.rewrite(rule, newContext).asInstanceOf[Typename],
        size.rewrite(rule, newContext).asInstanceOf[Expression]
      ), context)
  }
}

case class InstanceOf(
  lhs: Expression,
  tname: Typename
) extends Expression {
  val children = Seq(lhs, tname)

  def rewrite(rule: Rewriter, context: Seq[Visitable]) = {
    val newContext = this +: context
    rule(
      InstanceOf(
        lhs.rewrite(rule, newContext).asInstanceOf[Expression],
        tname.rewrite(rule, newContext).asInstanceOf[Typename]
      ), context)
  }
}

case class Assignment(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(rule: Rewriter, context: Seq[Visitable]) =
    rewriter(Assignment.apply _)(rule, context)
}

case class LogicOr(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(rule: Rewriter, context: Seq[Visitable]) =
    rewriter(LogicOr.apply _)(rule, context)
}

case class LogicAnd(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(rule: Rewriter, context: Seq[Visitable]) =
    rewriter(LogicAnd.apply _)(rule, context)
}

case class BitOr(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(rule: Rewriter, context: Seq[Visitable]) =
    rewriter(BitOr.apply _)(rule, context)
}

case class BitAnd(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(rule: Rewriter, context: Seq[Visitable]) =
    rewriter(BitAnd.apply _)(rule, context)
}

case class Eq(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(rule: Rewriter, context: Seq[Visitable]) =
    rewriter(Eq.apply _)(rule, context)
}

case class NEq(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(rule: Rewriter, context: Seq[Visitable]) =
    rewriter(NEq.apply _)(rule, context)
}

case class LEq(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(rule: Rewriter, context: Seq[Visitable]) =
    rewriter(LEq.apply _)(rule, context)
}

case class GEq(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(rule: Rewriter, context: Seq[Visitable]) =
    rewriter(GEq.apply _)(rule, context)
}

case class LThan(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(rule: Rewriter, context: Seq[Visitable]) =
    rewriter(LThan.apply _)(rule, context)
}

case class GThan(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(rule: Rewriter, context: Seq[Visitable]) =
    rewriter(GThan.apply _)(rule, context)
}

case class Add(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(rule: Rewriter, context: Seq[Visitable]) =
    rewriter(Add.apply _)(rule, context)
}

case class Sub(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(rule: Rewriter, context: Seq[Visitable]) =
    rewriter(Sub.apply _)(rule, context)
}

case class Mul(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(rule: Rewriter, context: Seq[Visitable]) =
    rewriter(Mul.apply _)(rule, context)
}

case class Div(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(rule: Rewriter, context: Seq[Visitable]) =
    rewriter(Div.apply _)(rule, context)
}

case class Mod(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(rule: Rewriter, context: Seq[Visitable]) =
    rewriter(Mod.apply _)(rule, context)
}

case class Index(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(rule: Rewriter, context: Seq[Visitable]) =
    rewriter(Index.apply _)(rule, context)
}

case class Member(lhs: Expression, rhs: Id) extends Expression {
  val children = Seq(lhs, rhs)

  def rewrite(rule: Rewriter, context: Seq[Visitable]) = {
    val newContext = this +: context
    rule(
      Member(
        lhs.rewrite(rule, newContext).asInstanceOf[Expression],
        rhs.rewrite(rule, newContext).asInstanceOf[Id]
      ), context)
  }

  def fold[T](rule: Expression => T): Seq[T] = {
    def lifted(ghs: Expression): Seq[T] =
      ghs match {
        case member: Member => member.fold(rule)
        case otherwise      => Seq(rule(otherwise))
      }

    lifted(lhs) ++ lifted(rhs)
  }
}

case class StaticMember(lhs: ClassDefn, rhs: Expression) extends Expression {
  val children = Seq(rhs)

  def rewrite(rule: Rewriter, context: Seq[Visitable]) = {
    val newContext = this +: context
    rule(
      StaticMember(
        lhs.rewrite(rule, newContext).asInstanceOf[ClassDefn],
        rhs.rewrite(rule, newContext).asInstanceOf[Expression]
      ), context)
  }
}

case class Not(ghs: Expression) extends UnOp {
  def rewrite(rule: Rewriter, context: Seq[Visitable]) =
    rewriter(Not.apply _)(rule, context)
}

case class Neg(ghs: Expression) extends UnOp {
  def rewrite(rule: Rewriter, context: Seq[Visitable]) =
    rewriter(Neg.apply _)(rule, context)
}
