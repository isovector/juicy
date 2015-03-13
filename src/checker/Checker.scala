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
  def checkMod(flags: Modifiers.Value, flag: Modifiers.Value) = (flags & flag) == flag
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
  def apply(node: FileNode, pkgTree: PackageTree): FileNode = {
    var errors = Seq[CompilerError]()
    
    val numerics = Map[Typename, NumericType.value](
      (pkgTree.getTypename(Seq("int")).get -> NumericType.INT),
      (pkgTree.getTypename(Seq("java", "lang", "Integer")).get -> NumericType.INT),
      (pkgTree.getTypename(Seq("short")).get -> NumericType.SHORT),
      (pkgTree.getTypename(Seq("java", "lang", "Short")).get -> NumericType.SHORT),
      (pkgTree.getTypename(Seq("byte")).get -> NumericType.BYTE),
      (pkgTree.getTypename(Seq("java", "lang", "Byte")).get -> NumericType.BYTE),
      (pkgTree.getTypename(Seq("char")).get -> NumericType.CHAR),
      (pkgTree.getTypename(Seq("java", "lang", "Character")).get -> NumericType.CHAR)
    )
    
    def isWidening(lhs: Typename, rhs: Typename): Boolean = {
      val lhsT = numerics(lhs)
      val rhsT = numerics(rhs)
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
          case (l: CharVal, r: CharVal) => IntVal(fold(l.value, r.value))
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
    val BoolTypename = pkgTree.getTypename(Seq("boolean")).get
    val NullType = NullDefn().makeTypename
    val VoidType = pkgTree.getTypename(Seq("void")).get
    val thisCls = node.classes(0)
    val thisType = thisCls.makeTypename
    val ObjectType = pkgTree.getTypename(Seq("java", "lang", "Object"))
    
    
    def doComp(expr: BinOp, fold: (Int, Int) => Boolean, symbol: String): Expression = {
      if (expr.lhs.exprType.isEmpty || expr.rhs.exprType.isEmpty) {
        expr
      } else if ((numerics contains expr.lhs.exprType.get) && (numerics contains expr.rhs.exprType.get)) {
        val newExpr = (expr.lhs, expr.rhs) match {
          case (l: CharVal, r: CharVal) => BoolVal(fold(l.value, r.value))
          case (l: CharVal, r: IntVal) => BoolVal(fold(l.value, r.value))
          case (l: IntVal, r: CharVal) => BoolVal(fold(l.value, r.value))
          case (l: IntVal, r: IntVal) => BoolVal(fold(l.value, r.value))
          case _ => expr
        }
        newExpr.exprType = Some(BoolTypename)
        newExpr
      } else {
        errors :+= unsupported(symbol, expr.from, expr.lhs.exprType.get, expr.rhs.exprType.get)
        expr
      }
    }
    
    def hasProtectedAccess(t1: TypeDefn, t2: TypeDefn): Boolean = {
      return ((t1 resolvesTo thisCls) && (t1 isSubtypeOf t2)) || (t1 resolvesTo t2) || (t2 isSubtypeOf t1) || (t1.pkg == t2.pkg)
    }
    
    def isAssignable(lhs: Typename, rhs: Typename): Boolean = {
      if (lhs.resolved.get resolvesTo rhs.resolved.get) {
        true
      } else if (lhs.isArray && rhs.isArray) {
         val elemL = lhs.resolved.map(_.asInstanceOf[ArrayDefn]).map(_.elemType).get
         val elemR = rhs.resolved.map(_.asInstanceOf[ArrayDefn]).map(_.elemType).get
         (elemL resolvesTo elemR) || (elemL.nullable && (elemR isSubtypeOf elemL))
      } else if ((numerics contains lhs) && (numerics contains rhs)) {
        isWidening(lhs, rhs)
      } else if (rhs == NullType && lhs.resolved.get.nullable) {
        true
      } else if (rhs.resolved.get isSubtypeOf lhs.resolved.get) {
        true
      } else {
        false
      }
    }
    
    val newFile = node.rewrite(Rewriter {(self, context) =>
      implicit val ctx = context
      self match {
        case i: Id =>
          val isVariable = context.head match {
            case Member(_, r) if r == i => false
            case sm: StaticMember => false
            case Callee(f) if f == i => false
            case v: VarStmnt => true
            case _ => true
          }
          if (isVariable) {
            val name = i.name
            val varScope =  (Seq(i.scope.get) ++ thisCls.superTypes.map(_.classScope)).find(_.resolve(name) != None)
            if (varScope.isEmpty) {
              errors :+= undefined(i, thisType)
            } else if (isIn[MethodDefn]()) {
              val isStatic = checkMod(ancestor[MethodDefn].map(_.mods).get, Modifiers.STATIC)
              val nonStaticVar = {
                if (i.scope == varScope) {
                  !i.scope.get.isLocalScope(name) && thisCls.fields.find(f => f.name == name && checkMod(f.mods, Modifiers.STATIC)).isEmpty
                } else {
                  thisCls.superTypes.find(s => s.fields.find(
                        f => f.name == name && !checkMod(f.mods, Modifiers.STATIC)).isDefined).isDefined
                }
              }
              if (isStatic && nonStaticVar) {
                errors :+= CheckerError(s"Reference to instance variable $name in static context", i.from)
              }
              i.exprType = varScope.flatMap(_.resolve(name))
            }
          }
          i
        case m@Member(left, right) =>
           val isInCall = context.head match {
             case c: Callee => true
             case _ => false
           }
            if (!isInCall && left.hasType) {
                val rname = right.name
                val curType = left.exprType.flatMap(_.resolved).get
                val definedIn = (curType +: curType.superTypes).find(_.classScope.resolve(rname) != None)
                if (definedIn.isEmpty) {
                  errors :+= undefined(right, left.exprType.get)
                } else {
                  val field = definedIn.get.fields.filter(_.name == rname)(0)
                  if (checkMod(field.mods, Modifiers.PROTECTED) && !hasProtectedAccess(thisCls, curType)) {
                    errors :+= protectedAccess(right.name, thisType, curType.makeTypename)
                  }
                  m.exprType = definedIn.get.classScope.resolve(rname)
                }
            }
          m
        case c@Call(method, fields) =>
          val (cls, ident, isStatic) = method.expr match {
            case id: Id => (Some(thisCls), id.name, Some(false))
            case StaticMember(cls, right) => (Some(cls), right.name, Some(true))
            case Member(left, right) =>  (left.exprType.flatMap(_.resolved), right.name, Some(false))
            case e: Expression => throw new CheckerError(s"How the fuck did $e you get here?", e.from)
          }
          if (cls.isDefined) {
            if(fields.filter(!_.hasType).isEmpty) {
              val argtypes = fields.map(_.exprType.get)
              val sig = Signature(ident, argtypes)
              val declCls = cls.get.origTypeForMethod(sig)
              val tn = declCls.flatMap(m => m.methods.find(_.signature == sig))
              if (tn.isEmpty) {
                val at = argtypes.mkString(",")
                errors :+= CheckerError(s"No method $ident defined for parameters: $at", c.from)
              } else {
                if(isStatic.isDefined) {
                  if (isStatic.get && !checkMod(tn.get.mods, Modifiers.STATIC)) {
                    errors :+= CheckerError(s"Nonstatic method $ident accessed from a static context", c.from)
                  } else if (!isStatic.get && checkMod(tn.get.mods, Modifiers.STATIC)) {
                    errors :+= CheckerError(s"Static method $ident accessed from a nonstatic context", c.from)
                  }
                }
                if (checkMod(tn.get.mods, Modifiers.PROTECTED)) {
                  if (checkMod(tn.get.mods, Modifiers.STATIC) && !hasProtectedAccess(thisCls, declCls.get)) {
                    println(thisCls.name, declCls.get.name)
                    errors :+= protectedAccess(ident, thisType, cls.get.makeTypename)
                  }
                  else if (!checkMod(tn.get.mods, Modifiers.STATIC) && !hasProtectedAccess(thisCls, declCls.get)) {
                    errors :+= protectedAccess(ident, thisType, declCls.get.makeTypename)
                  }
                }
                c.exprType = Some(tn.get.tname)
              }
            }
          }
          c
        case i: IntVal =>
          i.exprType = pkgTree.getTypename(Seq("int"))
          i
        case b: BoolVal =>
          b.exprType = Some(BoolTypename)
          b
        case c: CharVal =>
          c.exprType = pkgTree.getTypename(Seq("char"))
          c
        case s: StringVal =>
          s.exprType = Some(StringTypename)
          s
        case n: NullVal =>
          n.exprType = Some(NullType)
          n
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
        case eq: Eq => {
          if (!eq.lhs.hasType || !eq.rhs.hasType) {
            eq
          } else if (eq.lhs.exprType.get == VoidType || eq.rhs.exprType.get == VoidType) {
            errors :+= unsupported("==", eq.from, eq.lhs.exprType.get, eq.rhs.exprType.get)
            eq
          } else if ((numerics contains eq.lhs.exprType.get) && (numerics contains eq.rhs.exprType.get)) {
            doComp(eq, (a, b) => a == b, "==")
          } else if (eq.lhs.exprType.get == eq.rhs.exprType.get) {
            val expr = (eq.lhs, eq.rhs) match {
              case (s1: StringVal, s2: StringVal) => BoolVal(s1.value == s2.value)
              case (l, r) if l.exprType.get == NullType => BoolVal(true)
              case _ => eq
            }
            expr.exprType = Some(BoolTypename)
            expr
          } else if ((eq.lhs.exprType.get == NullType && eq.rhs.exprType.flatMap(_.resolved).get.nullable) 
              || (eq.rhs.exprType.get == NullType && eq.lhs.exprType.flatMap(_.resolved).get.nullable)) {
            eq.exprType = Some(BoolTypename)
            eq
          } else if ((eq.lhs.exprType.flatMap(_.resolved).get) isSubtypeOf (eq.rhs.exprType.flatMap(_.resolved).get)) {
            eq.exprType = Some(BoolTypename)
            eq
          } else if ((eq.rhs.exprType.flatMap(_.resolved).get) isSubtypeOf (eq.lhs.exprType.flatMap(_.resolved).get)) {
            eq.exprType = Some(BoolTypename)
            eq
          } else {
            errors :+= unsupported("==", eq.from, eq.lhs.exprType.get, eq.rhs.exprType.get)
            eq
          }
        }
        case neq: NEq => {
          if (!neq.lhs.hasType || !neq.rhs.hasType) {
            neq
          } else if ((numerics contains neq.lhs.exprType.get) && (numerics contains neq.rhs.exprType.get)) {
            doComp(neq, (a, b) => a != b, "!=")
          } else if (neq.lhs.exprType.get == neq.rhs.exprType.get) {
            val expr = (neq.lhs, neq.rhs) match {
              case (s1: StringVal, s2: StringVal) => BoolVal(s1.value == s2.value)
              case (l, r) if (l.exprType.get == NullType) => BoolVal(false)
              case _ => neq
            }
            expr.exprType = Some(BoolTypename)
            expr
          } else if ((neq.lhs.exprType.get == NullType && neq.rhs.exprType.flatMap(_.resolved).get.nullable) 
              || (neq.rhs.exprType.get == NullType && neq.lhs.exprType.flatMap(_.resolved).get.nullable)) {
            neq.exprType = Some(BoolTypename)
            neq
          } else if ((neq.lhs.exprType.flatMap(_.resolved).get) isSubtypeOf (neq.rhs.exprType.flatMap(_.resolved).get)) {
            neq.exprType = Some(BoolTypename)
            neq
          } else if ((neq.rhs.exprType.flatMap(_.resolved).get) isSubtypeOf (neq.lhs.exprType.flatMap(_.resolved).get)) {
            neq.exprType = Some(BoolTypename)
            neq
          } else {
            errors :+= unsupported("!=", neq.from, neq.lhs.exprType.get, neq.rhs.exprType.get)
            neq
          }
        }
        case inst: InstanceOf => {
          if (!inst.lhs.hasType) {
            inst
          } else if (isAssignable(inst.tname, inst.lhs.exprType.get)){
             val expr = BoolVal(true)
             expr.exprType = Some(BoolTypename)
             expr
          } else if (isAssignable(inst.lhs.exprType.get, inst.tname)) {
            inst.exprType = Some(BoolTypename)
            inst
          } else {
            errors :+= unsupported("instanceof", inst.from, inst.lhs.exprType.get, inst.tname)
            inst
          }
        }
        case nt: NewType => {
          nt.exprType = Some(nt.tname)
          val args = nt.args.map(_.exprType)
          if(args.filter(_.isEmpty).isEmpty) {
            val defArgs = args.map(_.get)
            val cxrs = nt.tname.resolved.get.methods.filter(_.isCxr).map(_.signature).filter(_.params == defArgs)
            if (cxrs.isEmpty) {
              val arglist = defArgs.mkString(",")
              val t = nt.tname
              errors :+= CheckerError(s"No constructor for type $t with parameters $arglist", nt.from)
            } else if (checkMod(nt.tname.resolved.map(_.mods).get, Modifiers.ABSTRACT) || nt.tname.resolved.map(_.isInterface).get) {
              val t = nt.tname
              errors :+= CheckerError(s"Instantiation of non-concrete type $t", nt.from)
            }
          }
          nt
        }
        case narr: NewArray => {
          if (narr.size.exprType.isEmpty) {
            // Already invalid
          } else if (numerics contains narr.size.exprType.get) {
            narr.exprType = Some(narr.tname)
          } else {
            val t = narr.size.exprType.get.name
            errors :+= CheckerError(s"Array size cannot be of type $t", narr.from)
          }
          narr
        }
        case geq: GEq => doComp(geq, (a,b) => a >= b, ">=")
        case gt: GThan => doComp(gt, (a,b) => a > b, ">")
        case leq: LEq => doComp(leq, (a,b) => a <= b, "<=")
        case lt: LThan => doComp(lt, (a,b) => a < b, "<")
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
              case (b1: BoolVal, b2) if b1.value => b2
              case (b1, b2: BoolVal) if b2.value => b1
              case _ => and
            }
            res.exprType = Some(BoolTypename)
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
            res.exprType = Some(BoolTypename)
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
            newVal.exprType = Some(BoolTypename)
            newVal
          } else {
            errors :+= unsupported("unary-!", n.from, n.ghs.exprType.get)
            n
          }
        case ass: Assignment =>
          if (ass.lhs.exprType.isEmpty || ass.rhs.exprType.isEmpty) {
            ass
          } else if (ass.lhs.exprType.get.isFinal) {
            val tname = ass.lhs
            errors :+= CheckerError(s"Invalid assignment to final expression $tname", ass.from)
            ass
          } else if (isAssignable(ass.lhs.exprType.get, ass.rhs.exprType.get)) {
            ass.exprType = ass.lhs.exprType
            ass
          } else {
            errors :+= unsupported("assignment", ass.from, ass.lhs.exprType.get, ass.rhs.exprType.get)
            ass
          }
        case v: VarStmnt =>
          if (v.value.isDefined && v.value.flatMap(_.exprType).isDefined) {
            println("Assign ", v.tname, v.name, v.value.flatMap(_.exprType))
            if (!isAssignable(v.tname, v.value.flatMap(_.exprType).get)) {
              errors :+= unsupported("assignment", v.from, v.tname, v.value.flatMap(_.exprType).get)
            }
          }
          v
        case i: IfStmnt =>
          if (i.cond.exprType.isEmpty) {
              i
          } else if (bools contains i.cond.exprType.get) {
            i.cond match {
               case BoolVal(false) => i.otherwise.getOrElse(BlockStmnt(Seq()))
               case BoolVal(true) => i.then
               case _ => i
            }
          } else {
            val t = i.cond.exprType.map(_.qname.mkString(".")).get
            errors :+= CheckerError(s"Branch Condition must be a boolean, received $t", i.from)
            i
          }
        case f: ForStmnt =>
          if (f.cond.isDefined && f.cond.flatMap(_.exprType).isDefined &&
              (!(bools contains f.cond.flatMap(_.exprType).get))) {
            val t = f.cond.flatMap(_.exprType).map(_.qname.mkString(".")).get
            errors :+= CheckerError(s"Loop condition must be a boolean, received $t", f.from)
          }
          f
        case w: WhileStmnt =>
          if (w.cond.exprType.isDefined && !(bools contains w.cond.exprType.get)) {
            val t = w.cond.exprType.map(_.qname.mkString(".")).get
            errors :+= CheckerError(s"Loop condition must be a boolean, received $t", w.from)
          }
          w
        case c: Cast =>
          if (c.value.exprType.isDefined) {
            val castType = c.tname
            val exprType = c.value.exprType.get
            if (((numerics contains castType) && (numerics contains exprType)) || 
                isAssignable(castType, exprType) || isAssignable(exprType, castType)) {
              c.exprType = Some(castType)
              c
            } else {
              errors :+= unsupported("cast", c.from, castType, exprType)
              c
            }
          } else {
            c
          }
        case r: ReturnStmnt =>
          val methodType = ancestor[MethodDefn].map(_.tname).flatMap(_.resolved).map(_.makeTypename).get
          if (r.value.isDefined) {
            if (methodType == VoidType) {
              errors :+= CheckerError("Void method cannot return value", r.from)
            } else if (r.value.flatMap(_.exprType).isDefined) {
              val retType = r.value.flatMap(_.exprType).get
              if(!isAssignable(methodType, retType)) {
                println(ancestor[MethodDefn].map(m => (m.name, m.params)).get)
                val rt = retType.qname.mkString(".")
                val mt = methodType.qname.mkString(".")
                errors :+= CheckerError(s"Return type $rt cannot be converted to expected type $mt", r.from)
              }
            }
          } else if (methodType != VoidType) {
            errors :+= CheckerError(s"Non-void method must return a value", r.from)
          }
          r
        case c: Callee => c
        case sm: StaticMember =>
          if (context.head != Callee(sm)) {
            val eqCls = (sm.lhs +: sm.lhs.superTypes)
                            .find(s => !s.fields.filter(f => f.name == sm.rhs.name && checkMod(f.mods, Modifiers.STATIC)).isEmpty)
            val eqId = eqCls.flatMap(c => c.fields.find(_.name == sm.rhs.name))
            if (eqId.isEmpty) {
              errors :+= undefined(sm.rhs, sm.lhs.makeTypename)
            } else if (!checkMod(eqId.get.mods, Modifiers.STATIC)) {
              val name = sm.rhs.name
              errors :+= CheckerError(s"Accessing non-static member $name from static context", sm.from)
            } else if (checkMod(eqId.get.mods, Modifiers.PROTECTED) && !hasProtectedAccess(thisCls, eqCls.get)) {
              errors :+= protectedAccess(sm.rhs.name, thisType, sm.lhs.makeTypename)
            } else {
              sm.exprType = eqId.map(_.tname)
            }
          }
          sm
        case t: ThisVal => 
          if (isIn[MethodDefn]() && (ancestor[MethodDefn].map(_.mods).get & Modifiers.STATIC) != 0) {
            errors :+= CheckerError(s"Reference to `this` in static context", t.from)
          }
          t.exprType = ancestor[ClassDefn].map(_.makeTypename)
          t
        case ass: Assignee =>
          ass.exprType = ass.expr.exprType
          ass
        case ex: Expression => 
          errors :+= CheckerError(s"Did not typecheck expression $ex", ex.from)
          ex
        case _ => self
      }
    }).asInstanceOf[FileNode]
    if (!errors.isEmpty) {
      throw new VisitError(errors)
    }
    newFile
  }
}
