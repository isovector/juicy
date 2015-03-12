package juicy.source.checker

import juicy.source._
import juicy.source.ast._
import juicy.source.scoper._
import juicy.source.tokenizer._
import juicy.utils._
import juicy.utils.visitor._

case class CheckerError (msg: String, from: SourceLocation) extends CompilerError


object Checker {
  object NumericType {
    type value = Int
    final val UNKNOWN = -1
    final val INT = 0
    final val SHORT = 1
    final val BYTE = 2
    final val CHAR = 3
    
  }
    
  def undefined (v: Expression) = CheckerError(s"Undefined symbol $v", v.from)
  def unsupported(op: String, from: SourceLocation, tnames:Typename*) = 
    CheckerError(s"Unsupported $op for types $tnames", from)

  def apply(node: FileNode, pkgTree: PackageTree): Unit = {
    var errors = Seq[CompilerError]()
    /*
    var scopeMap = Map[Visitable, ClassScope]()

    def addSubScope(v: Visitable, tn: Typename) = {
      scopeMap += (v -> tn.resolved.flatMap(_.scope)
                          .getOrElse(new ClassScope())
                          .enclosingClass)
    }

    scopeMap += (ThisVal() -> node.classScope)
    */
    val numerics = Map(
      (pkgTree.getType(Seq("int")).get -> NumericType.INT),
      (pkgTree.getType(Seq("java", "lang", "Integer")).get -> NumericType.INT),
      (pkgTree.getType(Seq("short")) -> NumericType.SHORT),
      (pkgTree.getType(Seq("java", "lang", "Short")) -> NumericType.SHORT),
      (pkgTree.getType(Seq("byte")) -> NumericType.BYTE),
      (pkgTree.getType(Seq("java", "lang", "Byte")) -> NumericType.BYTE),
      (pkgTree.getType(Seq("char")) -> NumericType.CHAR),
      (pkgTree.getType(Seq("java", "lang", "Char")) -> NumericType.CHAR)
    )
    
    val bools = Set(pkgTree.getTypename(Seq("boolean")).get, 
      pkgTree.getTypename(Seq("java", "lang", "Boolean")).get)
    
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
        val newExpr = (expr.lhs, expr.rhs) match {
          case (l: IntVal, r: IntVal) => IntVal(fold(l.value, r.value))
          case (l: CharVal, r: CharVal) => CharVal(fold(l.value, r.value).toChar)
          case (l: IntVal, r: CharVal) => IntVal(fold(l.value, r.value))
          case (l: CharVal, r: IntVal) => IntVal(fold(l.value, r.value))
          case _ => expr
        }
        newExpr.exprType = expr.exprType
        newExpr
      } else {
        if (expr.lhs.exprType.isDefined && expr.rhs.exprType.isDefined) {
          errors :+= unsupported(symbol, expr.from, expr.lhs.exprType.get, expr.rhs.exprType.get)
        }
        expr
      }
    }
    val StringTypename = pkgTree.getTypename(Seq("java", "lang", "String")).get
    
    node.rewrite(Rewriter {(self, context) =>
      implicit val ctx = context
      self match {
        case i: Id =>
          val isVariable = context.head match {
            case Member(_, r) if r == i => false
            case StaticMember(_,_) => false
            case Callee(f) if f == i => false
            case v: VarStmnt => false
            case _ => true
          }
          if (isVariable) {
            val name = i.name
            val tn = i.scope.get.resolve(name)
            if (tn.isEmpty) {
              errors :+= undefined(i)
            } else {
              i.exprType = tn
            }
          }
          i
        case m@Member(left, right) =>
          val scope = left.typeScope
          if (scope.isDefined) {
            if (context.head != Callee(m)) {
                val rname = right.name
                val tn = scope.map(_.enclosingClass).flatMap(_.resolve(rname))

                if (tn.isEmpty) {
                  errors :+= undefined(right)
                } else {
                  m.exprType = tn
                }
            }
          }
          m
        case c@Call(method, fields) =>
          val (cls, ident, isStatic) = method.expr match {
            case id: Id => (Some(node.classes(0)), id.name, false)
            case StaticMember(cls, right) => (Some(cls), right.name, true)
            case Member(left, right) => (left.exprType.flatMap(_.resolved), right.name, false)
            case e: Expression => throw new CheckerError(s"How the fuck did $e you get here?", e.from)
          }
          if (cls.isDefined) {
            if(fields.filter(!_.hasType).isEmpty) {
              val sig = Signature(ident, fields.map(_.exprType.get))
              val tn = cls.get.allMethods.filter(_.signature == sig)
              if (tn.isEmpty) {
                errors :+= undefined(c)
              } else if(isStatic && (tn(0).mods & Modifiers.STATIC) == 0) {
                errors :+= CheckerError(s"Nonstatic method $ident accessed from a static context", c.from)
              } else {
                c.exprType = Some(tn(0).tname)
              }
            }
          }
          c
        case i: IntVal =>
          i.exprType = pkgTree.getTypename(Seq("int"))
          i
        case b: BoolVal =>
          b.exprType = pkgTree.getTypename(Seq("boolean"))
          b
        case c: CharVal =>
          c.exprType = pkgTree.getTypename(Seq("char"))
          c
        case s: StringVal =>
          s.exprType = pkgTree.getTypename(Seq("java", "lang", "string"))
          s
        case n: Neg => {
          if (n.ghs.exprType.isEmpty) {
            n
          } else if (numerics contains n.ghs.exprType.get) {
            val newNeg = n.ghs match {
              case i: IntVal => IntVal(-i.value)
              case c: CharVal => IntVal(-c.value)
              case _ => n
            }
            newNeg.exprType = n.ghs.exprType
            newNeg
          } else {
            errors :+= unsupported("unary -", n.from, n.ghs.exprType.get)
            n
          }
        }
        case m: Mul => doNumeric(m, (a, b) => a * b, "*")
        case d: Div => doNumeric(d, (a,b) => a / b, "/")
        case s: Sub => doNumeric(s, (a,b) => a - b, "-")
        case m: Mod => doNumeric(m, (a,b) => a % b, "%")
        case a: Add =>
          if (a.lhs.exprType.isEmpty || a.rhs.exprType.isEmpty) {
            a
          } else if (a.lhs.exprType.get == StringTypename && a.rhs.exprType.get == StringTypename) {
            val newString = (a.lhs, a.rhs) match {
              case (l: StringVal, r: StringVal) => StringVal(l.value + r.value)
              case _ => a
            }
            newString.exprType = Some(StringTypename)
            newString
          } else if (a.lhs.exprType.get == StringTypename) {
            //TODO: actually collapse string + nonstring
            a.exprType = Some(StringTypename)
            a
          } else if (a.rhs.exprType.get == StringTypename) {
            //TODO: collapse nonstring + string
            a.exprType = Some(StringTypename)
            a
          } else {
            doNumeric(a, (l, r) => l + r, "+")
          }
        case ind: Index =>
          if (ind.lhs.exprType.isEmpty || ind.rhs.exprType.isEmpty) {
            // Nothing to do
          } else if (!(numerics contains ind.rhs.exprType.get)) {
            errors :+= unsupported("[]", ind.from, ind.lhs.exprType.get, ind.rhs.exprType.get)
          } else {
            val t = ind.lhs.exprType.flatMap(_.resolved).get
            t match {
              case arr@ArrayDefn(elem) => ind.exprType = Some(elem.makeTypename)
              case _ => errors :+= unsupported("[]", ind.from, ind.lhs.exprType.get, ind.rhs.exprType.get)
            }
          }
          ind
        case and: LogicAnd =>
          if (and.lhs.exprType.isEmpty || and.rhs.exprType.isEmpty) {
            and
          } else if ((bools contains and.lhs.exprType.get) && (bools contains and.rhs.exprType.get)) {
            val res = (and.lhs, and.rhs) match {
              case (b1: BoolVal, b2: BoolVal) => BoolVal(b1.value && b2.value)
              case _ => and
            }
            res.exprType = pkgTree.getTypename(Seq("bool"))
            res
          } else {
            errors :+= unsupported("&&", and.from, and.lhs.exprType.get, and.rhs.exprType.get)
            and
          }
        case or: LogicOr =>
          if (or.lhs.exprType.isEmpty || or.rhs.exprType.isEmpty) {
            or
          } else if ((bools contains or.lhs.exprType.get) && (bools contains or.rhs.exprType.get)) {
            val res = (or.lhs, or.rhs) match {
              case (b1: BoolVal, b2: BoolVal) => BoolVal(b1.value || b2.value)
              case _ => or
            }
            res.exprType = pkgTree.getTypename(Seq("bool"))
            res
          } else {
            errors :+= unsupported("||", or.from, or.lhs.exprType.get, or.rhs.exprType.get)
            or
          }
        case n: Not =>
          if (n.ghs.exprType.isEmpty) {
            n
          } else if (bools contains n.ghs.exprType.get) {
            val newVal = n.ghs match {
              case b: BoolVal => BoolVal(!b.value)
              case _ => n
            }
            newVal.exprType = pkgTree.getTypename(Seq("bool"))
            newVal
          } else {
            errors :+= unsupported("unary-!", n.from, n.ghs.exprType.get)
            n
          }
        case _ => self
      }
    })
    if (!errors.isEmpty) {
      throw new VisitError(errors)
    }
  }
}
