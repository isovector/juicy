package juicy.source.checker

import juicy.source._
import juicy.source.ast._
import juicy.source.scoper._
import juicy.source.tokenizer._
import juicy.utils._
import juicy.utils.visitor._

case class CheckerError (msg: String, from: SourceLocation) extends CompilerError


object Checker {

  def apply(node: FileNode, pkgTree: PackageTree): FileNode = {
    import CheckerHelper._

    val helper = new CheckerHelper(pkgTree, node.classes(0))

    val newFile = node.rewrite(Rewriter {(self, context) =>
      implicit val ctx = context
      self match {
        case id: Id =>
          val isVariable = context.head match {
            case Member(_, r) if r == id => false
            case sm: StaticMember => false
            case Callee(f) if f == id => false
            case v: VarStmnt => true
            case _ => true
          }
          if (isVariable) {
            val name = id.name
            val varScope =  (Seq(id.scope.get) ++ helper.ThisCls.superTypes.map(_.classScope)).find(_.resolve(name) != None)
            if (varScope.isEmpty) {
              helper.addError(undefined(id, helper.ThisType))
            } else if (isIn[MethodDefn]()) {
              val isStatic = ancestor[MethodDefn].get.isStatic
              val nonStaticVar = {
                if (id.scope == varScope) {
                  !id.scope.get.isLocalScope(name) && helper.ThisCls.fields.find(f => f.name == name && f.isStatic).isEmpty
                } else {
                  helper.ThisCls.superTypes.find(s => s.fields.find(
                        f => f.name == name && !f.isStatic).isDefined).isDefined
                }
              }
              if (isStatic && nonStaticVar) {
                helper.addError(CheckerError(s"Reference to instance variable $name in static context", id.from))
              }
              helper.setType(id, varScope.flatMap(_.resolve(name)).get)
            }
          }
          id

        case member@Member(left, right) =>
           val isInCall = context.head match {
             case c: Callee => true
             case _ => false
           }
            if (!isInCall && left.hasType) {
                val rname = right.name
                val curType = left.exprType.flatMap(_.resolved).get
                val definedIn = (curType +: curType.superTypes).find(_.classScope.resolve(rname) != None)
                if (definedIn.isEmpty) {
                  helper.addError(undefined(right, left.exprType.get))
                } else {
                  val field = definedIn.get.fields.filter(_.name == rname)(0)
                  if (!field.isPublic && !hasInstanceProtectedAccess(helper.ThisCls, curType, definedIn.get)) {
                    helper.addError(protectedAccess(right.name, helper.ThisType, curType.makeTypename))
                  } else if (field.isStatic) {
                    helper.addError(CheckerError(s"Static Symbol $rname accessed from nonstatic context", member.from))
                  }
                  helper.setType(member, definedIn.get.classScope.resolve(rname).get)
                }
            }
          member

        case call@Call(method, fields) =>
          val (cls, ident, isStatic) = method.expr match {
            case id: Id => {
              val meth = ancestor[MethodDefn]
              val iname = id.name
              if (meth.isDefined && meth.get.isStatic) {
                 helper.addError(CheckerError(s"Nonstatic method $iname accessed from static context", call.from))
              }
              (Some(helper.ThisCls), iname, false)
            }
            // TODO: other setup labels
            case StaticMember(cls, right) => (Some(cls), right.name, true)
            case Member(left, right) =>  (left.exprType.flatMap(_.resolved), right.name, false)
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
                helper.addError(CheckerError(s"No method $ident defined for parameters: $at", call.from))
              } else {
                if (isStatic && !tn.get.isStatic) {
                    helper.addError(CheckerError(s"Nonstatic method $ident accessed from a static context", call.from))
                } else if (!isStatic && tn.get.isStatic) {
                    helper.addError(CheckerError(s"Static method $ident accessed from a nonstatic context", call.from))
                }
                if (!tn.get.isPublic && !(helper.ThisCls resolvesTo cls.get)) {
                  if (tn.get.isStatic && !hasStaticProtectedAccess(helper.ThisCls, cls.get, declCls.get)) {
                    helper.addError(protectedAccess(ident, helper.ThisType, cls.get.makeTypename))
                  }
                  else if (!tn.get.isStatic && !hasInstanceProtectedAccess(helper.ThisCls, cls.get, declCls.get)) {
                    helper.addError(protectedAccess(ident, helper.ThisType, declCls.get.makeTypename))
                  }
                }

                call.rawSignature = Some(sig)
                // TODO: only if it is static
                call.rawResolvedMethod = tn
                helper.setType(call, tn.get.tname)
              }
            }
          }
          call

        case int: IntVal =>
          helper.setInt(int)
          int

        case bool: BoolVal =>
          helper.setBoolean(bool)
          bool

        case char: CharVal =>
          helper.setChar(char)
          char

        case str: StringVal =>
          helper.setString(str)
          str

        case n: NullVal =>
          helper.setNull(n)
          n

        case neg@Neg(expr) => {
          if (!expr.hasType) {
            neg
          } else if (helper.isNumeric(expr)) {
            val newNeg = expr match {
              case i: IntVal => IntVal(-i.value)
              case c: CharVal => IntVal(-c.value)
              case _ => neg
            }
            newNeg.exprType = expr.exprType
            newNeg
          } else {
            helper.addError(unsupported("unary -", neg.from, expr.exprType.get))
            neg
          }
        }

        case eq@Eq(lhs, rhs) => {
          if (!lhs.hasType || !rhs.hasType) {
            eq
          } else if (helper.isVoid(lhs) || helper.isVoid(rhs)) {
            helper.addError(unsupported("==", eq.from, lhs.exprType.get, rhs.exprType.get))
            eq
          } else if (helper.isNumeric(lhs) && helper.isNumeric(rhs)) {
            helper.doComp(eq, (a, b) => a == b, "==")
          } else if (lhs.exprType.get == rhs.exprType.get) {
            val expr = (lhs, rhs) match {
              case (s1: StringVal, s2: StringVal) => BoolVal(s1.value == s2.value)
              case (b1: BoolVal, b2: BoolVal) => BoolVal(b1.value == b2.value)
              case (l, r) if helper.isNull(l) => BoolVal(true)
              case _ => eq
            }
            helper.setBoolean(expr)
            expr
          } else if (helper.isAssignable(lhs, rhs) || helper.isAssignable(rhs, lhs)) {
            helper.setBoolean(eq)
            eq
          } else {
            helper.addError(unsupported("==", eq.from, lhs.exprType.get, rhs.exprType.get))
            eq
          }
        }

        case neq@NEq(lhs, rhs) => {
          if (!lhs.hasType || !rhs.hasType) {
            helper.setBoolean(neq)
            neq
          } else if (helper.isNumeric(lhs) && helper.isNumeric(rhs)) {
            helper.doComp(neq, (a, b) => a != b, "!=")
          } else if (lhs.exprType.get == rhs.exprType.get) {
            val expr = (lhs, rhs) match {
              case (s1: StringVal, s2: StringVal) => BoolVal(s1.value != s2.value)
              case (b1: BoolVal, b2: BoolVal) => BoolVal(b1.value != b2.value)
              case (l, r) if helper.isNull(l) => BoolVal(false)
              case _ => neq
            }
            helper.setBoolean(expr)
            expr
          } else if (helper.isAssignable(lhs, rhs) || helper.isAssignable(rhs, lhs)) {
            helper.setBoolean(neq)
            neq
          } else {
            helper.addError(unsupported("!=", neq.from, lhs.exprType.get, rhs.exprType.get))
            helper.setBoolean(neq)
            neq
          }
        }

        case inst@InstanceOf(lhs, tname) => {
          if (!lhs.hasType) {
            helper.setBoolean(inst)
            inst
          } else if (!lhs.exprType.get.r.nullable ||
              !tname.r.nullable) {
            helper.addError(CheckerError("Operands of instanceOf cannot be value types", inst.from))
            helper.setBoolean(inst)
            inst
        } else if (helper.isAssignable(TypeExpression(tname), lhs)){
             val expr = BoolVal(true)
             helper.setBoolean(expr)
             expr
          } else if (helper.isAssignable(lhs, TypeExpression(tname))) {
            helper.setBoolean(inst)
            inst
          } else {
            helper.addError(unsupported("instanceof", inst.from, lhs.exprType.get, tname))
            helper.setBoolean(inst)
            inst
          }
        }

        case nt: NewType => {
          nt.exprType = Some(nt.tname)
          val args = nt.args.map(_.exprType)
          if(args.filter(_.isEmpty).isEmpty) {
            val defArgs = args.map(_.get)
            val cxr = nt.tname.resolved.get.methods.filter(_.isCxr).find(_.signature.params == defArgs)
            if (cxr.isEmpty) {
              val arglist = defArgs.mkString(",")
              val t = nt.tname
              helper.addError(CheckerError(s"No constructor for type $t with parameters $arglist", nt.from))
            } else if (nt.tname.r.isAbstract || nt.tname.r.isInterface) {
              val t = nt.tname
              helper.addError(CheckerError(s"Instantiation of non-concrete type $t", nt.from))
            } else if (!cxr.get.isPublic && nt.tname.resolved.map(_.pkg).get != helper.ThisCls.pkg) {
              val t = nt.tname
              helper.addError(CheckerError(s"Protected constructor for type $t cannot be accessed from outside package", nt.from))
            }

            nt.rawResolvedCxr = cxr
          }
          nt
        }

        case newArr@NewArray(tname, size) => {
          if (!size.hasType) {
            // Already invalid
          } else if (helper.isNumeric(size)) {
            helper.setType(newArr, tname)
          } else {
            val t = size.exprType.get.name
            helper.addError(CheckerError(s"Array size cannot be of type $t", newArr.from))
          }
          newArr
        }

        case geq: GEq => helper.doComp(geq, (a,b) => a >= b, ">=")

        case gt: GThan => helper.doComp(gt, (a,b) => a > b, ">")

        case leq: LEq => helper.doComp(leq, (a,b) => a <= b, "<=")

        case lt: LThan => helper.doComp(lt, (a,b) => a < b, "<")

        case mul: Mul => helper.doNumeric(mul, (a, b) => a * b, "*")

        case div: Div => helper.doNumeric(div, (a,b) => a / b, "/")

        case sub: Sub => helper.doNumeric(sub, (a,b) => a - b, "-")

        case mod: Mod => helper.doNumeric(mod, (a,b) => a % b, "%")

        case add@Add(lhs, rhs) =>
          if (!lhs.hasType || !rhs.hasType) {
            add
          } else if (helper.isString(lhs) && helper.isString(rhs)) {
            val newString = (lhs, rhs) match {
              case (l: StringVal, r: StringVal) => StringVal(l.value + r.value)
              case _ => StringConcat(lhs, rhs)
            }

            helper.setString(newString)
            newString
          } else if (helper.isString(lhs)) {
            if (helper.isVoid(rhs)) {
              helper.addError(unsupported("+", add.from, rhs.exprType.get))
              helper.setString(add)
              add
            } else {
              val strExpr = ToString(rhs)
              helper.setString(strExpr)
              val res = StringConcat(lhs, strExpr)
              helper.setString(res)
              res
            }
          } else if (helper.isString(rhs)) {
            if (helper.isVoid(lhs)) {
              helper.addError(unsupported("+", add.from, lhs.exprType.get))
              helper.setString(add)
              add
            } else {
              val strExpr = ToString(lhs)
              helper.setString(strExpr)
              val res = StringConcat(strExpr, rhs)
              res
            }
          } else {
            helper.doNumeric(add, (l, r) => l + r, "+")
          }

        case ind@Index(lhs, rhs) =>
          if (!lhs.hasType || !rhs.hasType) {
            // Nothing to do
          } else if (!helper.isNumeric(rhs)) {
            helper.addError(unsupported("[]", ind.from, lhs.exprType.get, rhs.exprType.get))
          } else {
            val t = lhs.exprType.get.r
            t match {
              case arr@ArrayDefn(elem) => helper.setType(ind, elem.makeTypename)
              case _ => helper.addError(unsupported("[]", ind.from, lhs.exprType.get, rhs.exprType.get))
            }
          }
          ind

        case and@LazyAnd(lhs, rhs) =>
          if (!lhs.hasType || !rhs.hasType) {
            and
          } else if (helper.isBoolean(lhs) && helper.isBoolean(rhs)) {
            val res = (lhs, rhs) match {
              case (b1: BoolVal, b2: BoolVal) => BoolVal(b1.value && b2.value)
              case (b1: BoolVal, b2) if b1.value => b2
              case (b1, b2: BoolVal) if b2.value => b1
              case _ => and
            }
            helper.setBoolean(res)
            res
          } else {
            helper.addError(unsupported("&&", and.from, lhs.exprType.get, rhs.exprType.get))
            and
          }

        case or@LazyOr(lhs, rhs) =>
          if (!lhs.hasType || !rhs.hasType) {
            or
          } else if (helper.isBoolean(lhs) && helper.isBoolean(rhs)) {
            val res = (lhs, rhs) match {
              case (b1: BoolVal, b2: BoolVal) => BoolVal(b1.value || b2.value)
              case _ => or
            }
            helper.setBoolean(res)
            res
          } else {
            helper.addError(unsupported("||", or.from, lhs.exprType.get, rhs.exprType.get))
            or
          }

        case n: Not =>
          if (!n.ghs.hasType) {
            n
          } else if (helper.isBoolean(n.ghs)) {
            val newVal = n.ghs match {
              case b: BoolVal => BoolVal(!b.value)
              case _ => n
            }
            helper.setBoolean(newVal)
            newVal
          } else {
            helper.addError(unsupported("unary-!", n.from, n.ghs.exprType.get))
            n
          }

        case ass@Assignment(lhs, rhs) =>
          if (!lhs.hasType || !rhs.hasType) {
            ass
          } else if (lhs.exprType.get.isFinal) {
            helper.addError(CheckerError(s"Invalid assignment to final expression $lhs", ass.from))
            ass
          } else if (helper.isAssignable(lhs, rhs)) {
            ass.exprType = lhs.exprType
            ass
          } else {
            helper.addError(unsupported("assignment", ass.from, lhs.exprType.get, rhs.exprType.get))
            ass
          }

        case v: VarStmnt =>
          if (v.value.isDefined && v.value.get.hasType) {
            if (!helper.isAssignable(TypeExpression(v.tname), v.value.get)) {
              helper.addError(unsupported("assignment", v.from, v.tname, v.value.flatMap(_.exprType).get))
            }
          }
          v

        case i: IfStmnt =>
          if (!i.cond.hasType) {
            i
          } else if (helper.isBoolean(i.cond)) {
            i
          } else {
            val t = i.cond.exprType.map(_.qname.mkString(".")).get
            helper.addError(CheckerError(s"Branch Condition must be a boolean, received $t", i.from))
            i
          }

        case f: ForStmnt =>
          if (f.cond.isDefined && f.cond.get.hasType &&
              (!(helper.isBoolean(f.cond.get)))) {
            val t = f.cond.flatMap(_.exprType).map(_.qname.mkString(".")).get
            helper.addError(CheckerError(s"Loop condition must be a boolean, received $t", f.from))
          }
          f

        case w: WhileStmnt =>
          if (w.cond.hasType && !helper.isBoolean(w.cond)) {
            val t = w.cond.exprType.map(_.qname.mkString(".")).get
            helper.addError(CheckerError(s"Loop condition must be a boolean, received $t", w.from))
          }
          w

        case c@Cast(tname, value) =>
          if (c.value.hasType) {
            val castType = TypeExpression(tname)
            if ((helper.isNumeric(castType) && helper.isNumeric(value)) ||
                helper.isAssignable(castType, value) || helper.isAssignable(value, castType)) {
              helper.setType(c, tname)
              c
            } else {
              helper.addError(unsupported("cast", c.from, tname, value.exprType.get))
              c
            }
          } else {
            c
          }

        case r: ReturnStmnt =>
          val methodType = TypeExpression(ancestor[MethodDefn].map(_.tname).flatMap(_.resolved).map(_.makeTypename).get)
          if (r.value.isDefined) {
            if (helper.isVoid(methodType)) {
              helper.addError(CheckerError("Void method cannot return value", r.from))
            } else if (r.value.flatMap(_.exprType).isDefined) {
              if(!helper.isAssignable(methodType, r.value.get)) {
                val rt = r.value.flatMap(_.exprType).get.qname.mkString(".")
                val mt = methodType.exprType.get.qname.mkString(".")
                helper.addError(CheckerError(s"Return type $rt cannot be converted to expected type $mt", r.from))
              }
            }
          } else if (!helper.isVoid(methodType)) {
            helper.addError(CheckerError(s"Non-void method must return a value", r.from))
          }
          r

        case c: Callee => c

        case sm@StaticMember(lhs, rhs) =>
          if (context.head != Callee(sm)) {
            val eqCls = (lhs +: lhs.superTypes)
                            .find(s => !s.fields.filter(f => f.name == rhs.name && f.isStatic).isEmpty)
            val eqId = eqCls.flatMap(c => c.fields.find(_.name == rhs.name))
            if (eqId.isEmpty) {
              helper.addError(undefined(rhs, lhs.makeTypename))
            } else if (!eqId.get.isStatic) {
              val name = rhs.name
              helper.addError(CheckerError(s"Accessing non-static member $name from static context", sm.from))
            } else if (!eqId.get.isPublic && !hasStaticProtectedAccess(helper.ThisCls, lhs, eqCls.get)) {
              helper.addError(protectedAccess(rhs.name, helper.ThisType, lhs.makeTypename))
            } else {
              helper.setType(sm, eqId.get.tname)
            }
          }
          sm

        case t: ThisVal =>
          if (isIn[MethodDefn]() && ancestor[MethodDefn].get.isStatic) {
            helper.addError(CheckerError(s"Reference to `this` in static context", t.from))
          }
          t.exprType = ancestor[ClassDefn].map(_.makeTypename)
          t

        case ass: Assignee =>
          ass.exprType = ass.expr.exprType
          ass

        case Parens(expr) =>
          expr

        case ex: Expression =>
          helper.addError(CheckerError(s"Did not typecheck expression $ex", ex.from))
          ex

        case _ => self
      }
    }).asInstanceOf[FileNode]
    helper.throwIfErrors
    newFile
  }
}
