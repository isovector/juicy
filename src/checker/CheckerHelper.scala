package juicy.source.checker

import juicy.source.PackageTree
import juicy.source.tokenizer.SourceLocation
import juicy.source.ast._
import juicy.utils.CompilerError
import juicy.utils.visitor.VisitError

object NumericType {
  type value = Int
  final val UNKNOWN = -1
  final val INT = 0
  final val SHORT = 1
  final val BYTE = 2
  final val CHAR = 3
}

object CheckerHelper {
    def hasStaticProtectedAccess(t1: TypeDefn, tref: TypeDefn, tdef: TypeDefn): Boolean = {
      return ((t1 isSubtypeOf tdef)) || (t1.pkg == tdef.pkg)
    }

    def hasInstanceProtectedAccess(t1: TypeDefn, tref: TypeDefn, tdef: TypeDefn): Boolean = {
      return ((tref isSubtypeOf t1) && (t1 isSubtypeOf tdef)) || (t1.pkg == tdef.pkg)
    }
    
  def undefined (v: Id, t: Typename) = {
    val tn = t.name
    val in = v.name
    CheckerError(s"Undefined symbol $in for type $tn", v.from)
  }
  def protectedAccess (v: String, t1: Typename, t2: Typename) = {
    val tn1 = t1.name
    val tn2 = t2.name
    CheckerError(s"Type $tn1 does not have access to symbol $v in type $tn2", t2.from)
  }
  
  def unsupported(op: String, from: SourceLocation, tnames:Typename*) = {
    val ts = tnames.mkString(", ")
    val s = if (tnames.length == 1) "" else "s"
    CheckerError(s"Unsupported $op for type$s $ts", from)
  }
}

class CheckerHelper (pkgTree: PackageTree, val ThisCls: ClassDefn) {
    import CheckerHelper._
    var errors = Seq[CompilerError]()
    
    private val numerics = Map[Typename, NumericType.value](
      (pkgTree.getTypename(Seq("int")).get -> NumericType.INT),
      (pkgTree.getTypename(Seq("java", "lang", "Integer")).get -> NumericType.INT),
      (pkgTree.getTypename(Seq("short")).get -> NumericType.SHORT),
      (pkgTree.getTypename(Seq("java", "lang", "Short")).get -> NumericType.SHORT),
      (pkgTree.getTypename(Seq("byte")).get -> NumericType.BYTE),
      (pkgTree.getTypename(Seq("java", "lang", "Byte")).get -> NumericType.BYTE),
      (pkgTree.getTypename(Seq("char")).get -> NumericType.CHAR),
      (pkgTree.getTypename(Seq("java", "lang", "Character")).get -> NumericType.CHAR)
    )
    private val bools = Set(pkgTree.getTypename(Seq("boolean")).get,
      pkgTree.getTypename(Seq("java", "lang", "Boolean")).get)
 
    private val primitives: Map[Typename, Typename] = Map(
      "boolean" -> Seq("java", "lang", "Boolean"), 
      "byte" -> Seq("java", "lang", "Byte"), 
      "char" -> Seq("java", "lang", "Character"),
      "int" -> Seq("java", "lang", "Integer"), 
      "short" -> Seq("java", "lang", "Short")
    ).map(_ match {
      case (tnp, tnr) => pkgTree.getTypename(Seq(tnp)).get -> pkgTree.getTypename(tnr).get
    })
    
    private val primitiveStrs = Map(
      "boolean" -> { e => BoolToStr(e)},
      "byte" -> { e => ByteToStr(e)},
      "char" -> { e => CharToStr(e)},
      "int" -> { e => IntToStr(e)},
      "short" -> { e => ShortToStr(e)}
    ).map(_ match {
      case (tn, fn) => (pkgTree.getTypename(Seq(tn)).get -> fn)
    })
      
    private val StringType = pkgTree.getTypename(Seq("java", "lang", "String")).get
    private val BoolType = pkgTree.getTypename(Seq("boolean")).get
    private val NullType = NullDefn().makeTypename
    private val VoidType = pkgTree.getTypename(Seq("void")).get
    val ThisType = ThisCls.makeTypename
    private val IntType = pkgTree.getTypename(Seq("int")).get
    private val CharType = pkgTree.getTypename(Seq("char")).get
    private val ObjectCls = pkgTree.getType(Seq("java", "lang", "Object")).get
    
    def isNumeric(e: Expression) = numerics contains e.et
    def isPrimitive(e: Expression) = primitives contains e.et
    def isBoolean(e: Expression) = bools contains e.et
    def isString(e: Expression) = e.exprType == Some(StringType)
    def isVoid(e: Expression) = e.exprType == Some(VoidType)
    def isNull(e: Expression) = e.exprType == Some(NullType)
    
    def setInt(e: Expression) = {
      e.exprType = Some(IntType)
    }
    
    def setChar(e: Expression) = {
      e.exprType = Some(CharType)
    }
    
    def setString(e: Expression) = {
      e.exprType = Some(StringType)
    }
    
    def setNull(e: Expression)  = {
      e.exprType = Some(NullType)
    }
    
    def setBoolean(e: Expression) = {
      e.exprType = Some(BoolType)
    }
    
    def setVoid(e: Expression) = {
      e.exprType = Some(VoidType)
    }
    
    def setType(e: Expression, t: Typename) = {
      e.exprType = Some(t)
    }

    def doNumeric(expr: BinOp, fold: (Int, Int) => Int, symbol: String): Expression = {
      val lhsT = expr.lhs.exprType.flatMap(numerics.get _).getOrElse(NumericType.UNKNOWN)
      val rhsT = expr.rhs.exprType.flatMap(numerics.get _).getOrElse(NumericType.UNKNOWN)
      expr.exprType = (lhsT, rhsT) match {
        case (_, NumericType.UNKNOWN) => None
        case (NumericType.UNKNOWN, _) => None
        case (a, b) if a > b => expr.rhs.exprType
        case _ => expr.lhs.exprType
      }
      if (expr.exprType.isDefined) {
        val collapsed = try { 
          (expr.lhs, expr.rhs) match {
            case (l: IntVal, r: IntVal) => Some(IntVal(fold(l.value, r.value)))
            case (l: CharVal, r: CharVal) => Some(IntVal(fold(l.value, r.value)))
            case (l: IntVal, r: CharVal) => Some(IntVal(fold(l.value, r.value)))
            case (l: CharVal, r: IntVal) => Some(IntVal(fold(l.value, r.value)))
            case _ => None
          }
        } catch {
          case _: Exception => None
        }
        if (collapsed.isDefined) {
          collapsed.get.exprType = Some(IntType)
          collapsed.get
        } else {
          expr
        }
      } else {
        if (expr.lhs.exprType.isDefined && expr.rhs.exprType.isDefined) {
          addError(unsupported(symbol, expr.from, expr.lhs.et, expr.rhs.et))
        }
        expr
      }
    }
    
    def doComp(expr: BinOp, fold: (Int, Int) => Boolean, symbol: String): Expression = {
      if (expr.lhs.exprType.isEmpty || expr.rhs.exprType.isEmpty) {
        setBoolean(expr)
        expr
      } else if (isNumeric(expr.lhs) && isNumeric(expr.rhs)) {
        val newExpr = (expr.lhs, expr.rhs) match {
          case (l: CharVal, r: CharVal) => BoolVal(fold(l.value, r.value))
          case (l: CharVal, r: IntVal) => BoolVal(fold(l.value, r.value))
          case (l: IntVal, r: CharVal) => BoolVal(fold(l.value, r.value))
          case (l: IntVal, r: IntVal) => BoolVal(fold(l.value, r.value))
          case _ => expr
        }
        setBoolean(newExpr)
        newExpr
      } else {
        addError(unsupported(symbol, expr.from, expr.lhs.et, expr.rhs.et))
        setBoolean(expr)
        expr
      }
    }
    
    def isWidening(lhs: Expression, rhs: Expression): Boolean = {
      val lhsT = numerics(lhs.et)
      val rhsT = numerics(rhs.et)
      if (lhsT == rhsT) {
        true
      } else if (lhsT == NumericType.INT) {
        true
      } else if (lhsT == NumericType.SHORT && rhsT == NumericType.BYTE) {
        true
      } else {
        false
      }
    }

    def isArrayType(t: TypeDefn) = t match {
      case _: ArrayDefn => true
      case _ => false
    }

    def isAssignable(lhs: Expression, rhs: Expression): Boolean = {
      val lhsT = lhs.t
      val rhsT = rhs.t
      if (lhsT resolvesTo rhsT) {
        true
      } else if (lhsT resolvesTo ObjectCls) {
        true
      } else if (isArrayType(lhsT) && isArrayType(rhsT)) {
         val elemL = lhsT.asInstanceOf[ArrayDefn].elemType
         val elemR = rhsT.asInstanceOf[ArrayDefn].elemType
         (elemL resolvesTo elemR) || (elemL.nullable && (elemR isSubtypeOf elemL))
      } else if (isNumeric(lhs) && isNumeric(rhs)) {
        isWidening(lhs, rhs)
      } else if (isNull(rhs) && lhsT.nullable) {
        true
      } else if (rhsT isSubtypeOf lhsT) {
        true
      } else {
        false
      }
    }
    
    def wrapAsString(e: Expression): Expression = {
      val strVal = if (isPrimitive(e)) {
        primitiveStrs(e.et)(e)
      } else if (isNull(e)) {
        val sub = StringVal("null")
        setString(sub)
        StrToStr(sub)
      } else {
        e match {
          case lit: StringVal => {
            setString(lit)
            StrToStr(lit)
          }
          case _ => RefToStr(e)
        }
      }
      setString(strVal)
      strVal
    }
    
    def addError (err: CompilerError) {
      errors :+= err
    }
    def throwIfErrors() = {
      if (!errors.isEmpty) {
        throw new VisitError(errors)
      }
    }
}