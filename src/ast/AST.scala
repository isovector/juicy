package juicy.source.ast

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

trait Expression extends Visitable
trait Statement extends Visitable
trait Definition extends Visitable
trait ImportStmnt extends Statement

trait BinOp extends Expression {
  val lhs: Expression
  val rhs: Expression

  protected def rewriter[T <: BinOp]
      (ctor: (Expression, Expression) => T)
      (implicit rule: Rewriter) = {
    rule(
      ctor(
        lhs.rewrite.asInstanceOf[Expression],
        rhs.rewrite.asInstanceOf[Expression]
      ))
  }

  val children = Seq(lhs, rhs)
}

trait UnOp extends Expression {
  val ghs: Expression

  protected def rewriter[T <: UnOp]
      (ctor: Expression => T)
      (implicit rule: Rewriter) = {
    rule(
      ctor(
        ghs.rewrite.asInstanceOf[Expression]
      ))
  }

  val children = Seq(ghs)
}

case class Typename (qname: QName, isArray: Boolean=false) extends Visitable {
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

  def rewrite(implicit rule: Rewriter) = this
}

case class FileNode(
  pkg: QName,
  imports: Seq[ImportStmnt],
  classes: Seq[ClassDefn]
) extends Visitable {
  val children = imports ++ classes
  var importTable: Option[Map[QName, ClassDefn]] = None

  def rewrite(implicit rule: Rewriter) =
    rule(
      FileNode(
        pkg,
        imports.map(_.rewrite.asInstanceOf[ImportStmnt]),
        classes.map(_.rewrite.asInstanceOf[ClassDefn])
      ))
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

  def rewrite(implicit rule: Rewriter) =
    rule(
      ClassDefn(
        name,
        pkg,
        mods,
        extnds.map(_.rewrite.asInstanceOf[Typename]),
        impls.map(_.rewrite.asInstanceOf[Typename]),
        fields.map(_.rewrite.asInstanceOf[VarStmnt]),
        methods.map(_.rewrite.asInstanceOf[MethodDefn]),
        isInterface
      ))
  def resolvesTo (other: ClassDefn) =
    name == other.name && pkg == other.pkg
}

case class ImportClass(
  tname: Typename
) extends ImportStmnt {
  val children = Seq(tname)

  def rewrite(implicit rule: Rewriter) =
    rule(
      ImportClass(
        tname.rewrite.asInstanceOf[Typename]
      ))
}

case class ImportPkg(
  pkg: QName
) extends ImportStmnt {
  val children = Seq()

  def rewrite(implicit rule: Rewriter) = this
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

  def rewrite(implicit rule: Rewriter) =
    rule(
      MethodDefn(
        name,
        mods,
        isCxr,
        tname.rewrite.asInstanceOf[Typename],
        params.map(_.rewrite.asInstanceOf[VarStmnt]),
        body.map(_.rewrite.asInstanceOf[Statement])
      ))
}

case class VarStmnt(
  name: String,
  mods: Modifiers.Value,
  tname: Typename,
  value: Option[Expression]
) extends Statement {
  val children = tname +: value.toList

  def rewrite(implicit rule: Rewriter) =
    rule(
      VarStmnt(
        name,
        mods,
        tname.rewrite.asInstanceOf[Typename],
        value.map(_.rewrite.asInstanceOf[Expression])
      ))
}

case class IfStmnt(
  cond: Expression,
  then: BlockStmnt,
  otherwise: Option[BlockStmnt]
) extends Statement {
  val children = Seq(cond, then) ++ otherwise.toList

  def rewrite(implicit rule: Rewriter) =
    rule(
      IfStmnt(
        cond.rewrite.asInstanceOf[Expression],
        then.rewrite.asInstanceOf[BlockStmnt],
        otherwise.map(_.rewrite.asInstanceOf[BlockStmnt])
      ))
}

case class WhileStmnt(
  cond: Expression,
  body: Statement
) extends Statement {
  val children = Seq(cond, body)

  def rewrite(implicit rule: Rewriter) =
    rule(
      WhileStmnt(
        cond.rewrite.asInstanceOf[Expression],
        body.rewrite.asInstanceOf[Statement]
      ))
}

case class ForStmnt(
  first: Option[Statement],
  cond: Option[Expression],
  after: Option[Expression],
  body: Statement
) extends Statement {
  val children = first.toList ++ cond.toList ++ after.toList ++ Seq(body)

  def rewrite(implicit rule: Rewriter) =
    rule(
      ForStmnt(
        first.map(_.rewrite.asInstanceOf[Statement]),
        cond.map(_.rewrite.asInstanceOf[Expression]),
        after.map(_.rewrite.asInstanceOf[Expression]),
        body.rewrite.asInstanceOf[Statement]
      ))
}

case class BlockStmnt(
  body: Seq[Statement]
) extends Statement {
  val children = body

  def rewrite(implicit rule: Rewriter) =
    rule(
      BlockStmnt(
        body.map(_.rewrite.asInstanceOf[Statement])
      ))
}

case class ReturnStmnt(
  value: Expression
) extends Statement {
  val children = Seq(value)

  def rewrite(implicit rule: Rewriter) =
    rule(
      ReturnStmnt(
        value.rewrite.asInstanceOf[Expression]
      ))
}

case class ExprStmnt(
  expr: Expression
) extends Statement {
  val children = Seq(expr)

  def rewrite(implicit rule: Rewriter) =
    rule(
      ExprStmnt(
        expr.rewrite.asInstanceOf[Expression]
      ))
}

trait NullOp extends Expression {
  val children = Seq()

  def rewrite(implicit rule: Rewriter) = rule(this)
}

case class NullVal()                extends NullOp
case class ThisVal()                extends NullOp
case class SuperVal()               extends NullOp
case class Id(name: String)         extends NullOp
case class IntVal(value: Int)       extends NullOp
case class CharVal(value: Char)     extends NullOp
case class BoolVal(value: Boolean)  extends NullOp
case class StringVal(value: String) extends NullOp

case class Call(
  method: Expression,
  args: Seq[Expression]
) extends Expression {
  val children = args :+ method

  def rewrite(implicit rule: Rewriter) =
    rule(
      Call(
        method.rewrite.asInstanceOf[Expression],
        args.map(_.rewrite.asInstanceOf[Expression])
      ))
}

case class Cast(
  tname: Typename,
  value: Expression
) extends Expression {
  val children = Seq(tname, value)

  def rewrite(implicit rule: Rewriter) =
    rule(
      Cast(
        tname.rewrite.asInstanceOf[Typename],
        value.rewrite.asInstanceOf[Expression]
      ))
}

case class NewType(
  tname: Typename,
  args: Seq[Expression]
) extends Expression {
  val children = tname +: args

  def rewrite(implicit rule: Rewriter) =
    rule(
      NewType(
        tname.rewrite.asInstanceOf[Typename],
        args.map(_.rewrite.asInstanceOf[Expression])
      ))
}

case class NewArray(
  tname: Typename,
  size: Expression
) extends Expression {
  val children = Seq(tname, size)

  def rewrite(implicit rule: Rewriter) =
    rule(
      NewArray(
        tname.rewrite.asInstanceOf[Typename],
        size.rewrite.asInstanceOf[Expression]
      ))
}

case class InstanceOf(
  lhs: Expression,
  tname: Typename
) extends Expression {
  val children = Seq(lhs, tname)

  def rewrite(implicit rule: Rewriter) =
    rule(
      InstanceOf(
        lhs.rewrite.asInstanceOf[Expression],
        tname.rewrite.asInstanceOf[Typename]
      ))
}

case class Assignment(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(implicit rule: Rewriter) = rewriter(Assignment.apply _)
}

case class LogicOr(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(implicit rule: Rewriter) = rewriter(LogicOr.apply _)
}

case class LogicAnd(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(implicit rule: Rewriter) = rewriter(LogicAnd.apply _)
}

case class BitOr(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(implicit rule: Rewriter) = rewriter(BitOr.apply _)
}

case class BitAnd(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(implicit rule: Rewriter) = rewriter(BitAnd.apply _)
}

case class Eq(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(implicit rule: Rewriter) = rewriter(Eq.apply _)
}

case class NEq(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(implicit rule: Rewriter) = rewriter(NEq.apply _)
}

case class LEq(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(implicit rule: Rewriter) = rewriter(LEq.apply _)
}

case class GEq(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(implicit rule: Rewriter) = rewriter(GEq.apply _)
}

case class LThan(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(implicit rule: Rewriter) = rewriter(LThan.apply _)
}

case class GThan(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(implicit rule: Rewriter) = rewriter(GThan.apply _)
}

case class Add(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(implicit rule: Rewriter) = rewriter(Add.apply _)
}

case class Sub(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(implicit rule: Rewriter) = rewriter(Sub.apply _)
}

case class Mul(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(implicit rule: Rewriter) = rewriter(Mul.apply _)
}

case class Div(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(implicit rule: Rewriter) = rewriter(Div.apply _)
}

case class Mod(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(implicit rule: Rewriter) = rewriter(Mod.apply _)
}

case class Index(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(implicit rule: Rewriter) = rewriter(Index.apply _)
}

case class Member(lhs: Expression, rhs: Expression) extends BinOp {
  def rewrite(implicit rule: Rewriter) = rewriter(Member.apply _)

  def fold[T](rule: Expression => T): Seq[T] = {
    def lifted(ghs: Expression): Seq[T] =
      ghs match {
        case member: Member => member.fold(rule)
        case otherwise      => Seq(rule(otherwise))
      }

    lifted(lhs) ++ lifted(rhs)
  }
}

case class Not(ghs: Expression) extends UnOp {
  def rewrite(implicit rule: Rewriter) = rewriter(Not.apply _)
}

case class Neg(ghs: Expression) extends UnOp {
  def rewrite(implicit rule: Rewriter) = rewriter(Neg.apply _)
}
