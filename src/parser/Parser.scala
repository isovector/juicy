package juicy.source.parser

import juicy.source.ast._
import juicy.source.tokenizer._
import juicy.source.tokenizer.Token._
import juicy.utils.Implicits._
import juicy.utils.visitor._

class Parser(tokens: TokenStream) extends ParserUtils {
  import ParserUtils._

  def cur: Token = tokens.cur
  def next() = tokens.next()

  def operators = Seq(
    Map(
      "||" -> (LogicOr.tupled _)
    ),
    Map(
      "&&" -> (LogicAnd.tupled _)
    ),
    Map(
      "|" -> (BitOr.tupled _)
    ),
    Map(
      "&" -> (BitAnd.tupled _)
    ),
    Map(
      "==" -> (Eq.tupled _),
      "!=" -> (NEq.tupled _)
    ),
    Map(
      "<=" -> (LEq.tupled _),
      ">=" -> (GEq.tupled _),
      "<" -> (LThan.tupled _),
      ">" -> (GThan.tupled _),
      // HACK: stupid things are happening here. go look at
      // parseExprPrec
      "instanceof" -> (LEq.tupled _)
    ),
    Map(
      "+" -> (Add.tupled _),
      "-" -> (Sub.tupled _)
    ),
    Map(
      "*" -> (Mul.tupled _),
      "/" -> (Div.tupled _),
      "%" -> (Mod.tupled _)
    )
  )

  def consumeArray(): Boolean = {
      if (check("[")) {
          next()
          ensure("]")
          true
      } else {
          false
      }
  }

  def qualifiedName(): Typename = withSource {
    new Typename(
      if (checkPrimitive()) {
        Seq(unwrap(ensurePrimitive()))
      } else {
        delimited(".".asToken) {
          unwrap(ensureIdentifier())
        }
      }, consumeArray())
  }

  // Transform a list of identifiers into a left-assoc tree of member
  // accesses.
  def foldMemberAccess(ids: Seq[String]): Expression =
    ids
      .map(name => new Id(name))
      .reduceLeft[Expression](
        (lhs, rhs) => new Member(lhs, rhs))

  // Coalesce subsequent modifier tokens into a bitfield
  def parseModifiers(): Modifiers.Value = {
    val mods = tokens.takeWhile{
        case Modifier(name) => true
        case _              => false
    }
    mods.groupBy(l => l).map(t => (t._1, t._2.length)).foreach({ case (m, cnt) =>
      if (cnt > 1) {
          throw UnexpectedError("Duplicate modifier " + unwrap(m), m.from)
      }
    })
    
    (0 /: mods)((agg, token) => agg | Modifiers.parse(unwrap(token)))
  }

  // Outermost parser for a file
  def parseFile(): FileNode = withSource {
    // TODO: this probably shouldn't be so rigid
    val pkg =
      if (check("package")) {
        next()
        val tname = qualifiedName()
        if (tname.isArray) {
          tokens.rewind()
          throw Expected("valid package name")
        }

        ensure(";")
        tname.qname
      } else Seq()

    val children = kleene(new Token.EOF()) {
      if (check("import")) {
        parseImport()
      } else if (checkModifier() || check("class") || check("interface")) {
        val mods = parseModifiers()
        parseClass(mods)

      } else throw Expected("file-level declaration")
    }

    val (imports, classes) = children.partition { child =>
      child match {
        case (_: ImportStmnt) => true
        case (_: Definition)  => false
        case _ => throw Expected("file-level declarations")
      }
    }

    new FileNode(
      pkg,
      imports.map(_.asInstanceOf[ImportStmnt]),
      classes.map(_.asInstanceOf[ClassDefn])
    )
  }

  def parseClass(mods: Modifiers.Value): ClassDefn = withSource {
    val isInterface =
      if (check("interface")) {
        next()
        true
      } else {
        ensure("class")
        false
      }

    val name = unwrap(cur)
    next()

    val extnds =
      if (check("extends")) {
        next()
        Some(qualifiedName())
      } else None

    val impls = if (check("implements")) {
      next()
      delimited(",".asToken)(qualifiedName)
    } else Seq()

    ensure("{")
    val members = kleene("}".asToken)(parseClassMember(name))

    // Separate members into fields and methods
    val (fields, rawFunctionMembers) = members.partition { member =>
      member match {
        case (_: VarStmnt)   => true
        case (_: MethodDefn) => false
        case _ => throw Expected("field or method")
      }
    }

    val functionMembers = rawFunctionMembers.map(_.asInstanceOf[MethodDefn])

    val (constructors, methods) = functionMembers.partition { member =>
      member match {
        case method: MethodDefn if method.isConstructor => true
        case _                                          => false
      }
    }
    ensure("}")

    new ClassDefn(
      name,
      mods,
      extnds,
      impls,
      fields.map(_.asInstanceOf[VarStmnt]),
      constructors,
      methods,
      isInterface)
  }

  // Parse modifiers, types and names, and then delegate parsing to methods
  // or fields.
  def parseClassMember(className: String)(): Visitable = withSource {
    val mods = parseModifiers()
    val tname = qualifiedName()

    if (tname.toString == className && check("(")) {
      parseConstructor(className, mods)
    } else {
      val name = unwrap(ensureIdentifier())

      if (check("(")) {
        parseMethod(name, mods, tname)
      } else {
        parseField(name, mods, tname)
      }
    }
  }

  def parseParams(): Seq[VarStmnt] = {
    ensure("(")
    // While we don't hit a `)`, parse args delimited by `,`
    val params = kleene(")".asToken) {
      delimited(",".asToken) {
        val arg_tname = qualifiedName()
        val arg_name = unwrap(ensureIdentifier())
        new VarStmnt(arg_name, Modifiers.NONE, arg_tname, None)
      }
    }.flatMap(xs => xs) // flatten Seq(Seq()) to Seq()
    ensure(")")

    params
  }

  def parseConstructor(className: String, mods: Modifiers.Value) = {
    parseMethod(className, mods, new Typename(Seq(className)))
  }

  // Parse  `= Expr` or ``
  def parseInitializer(): Option[Expression] = {
    if (check("=")) {
      next()
      Some(parseExpr())
    } else None
  }

  // Parse a class field + potential initializer
  def parseField(
      name: String,
      mods: Modifiers.Value,
      tname: Typename): VarStmnt = withSource {
    val result = new VarStmnt(name, mods, tname, parseInitializer())
    ensure(";")
    result
  }

  def parseArgs(): Seq[Expression] = {
    ensure("(")
    val args = kleene(")".asToken) {
      delimited(",".asToken)(parseExpr)
    }.flatMap(xs => xs)
    ensure(")")

    args
  }

  def parseMethod(
      name: String,
      mods: Modifiers.Value,
      tname: Typename): MethodDefn = withSource {
    val params = parseParams()
    val body =
      if (!check(";"))
        Some(parseBlock())
      else {
        next()
        None
      }

    new MethodDefn(name, mods, tname, params, body)
  }

  // Parse `{ Statement[] }`
  def parseBlock(): BlockStmnt = withSource {
    ensure("{")
    val body = kleene("}".asToken)(parseStmnt)
    ensure("}")

    new BlockStmnt(body)
  }

  def parseStmnt(): Statement = withSource {
    if (check(";")) {
      next()
      new BlockStmnt(Seq())
    } else if (check("return")) {
      next();
      val value = parseExpr()
      ensure(";")

      new ReturnStmnt(value)
    } else if (check("while")) {
      parseWhile()
    } else if (check("for")) {
      parseFor()
    } else if (check("if")) {
      parseIf()
    } else if (check("{")) {
      parseBlock()
    } else if (checkIdentifier() || checkPrimitive()) {
      tokens.setBacktrace()
      try {
        val tname = qualifiedName()
        val name = unwrap(ensureIdentifier())
        val value = parseInitializer()
        ensure(";")

        tokens.unsetBacktrace()
        new VarStmnt(name, Modifiers.NONE, tname, value)
      } catch {
        case UnexpectedError(_, _) =>
          tokens.backtrack()
          parseExprStmnt()
      }
    } else parseExprStmnt()
  }

  def parseExprStmnt(): ExprStmnt = withSource {
    val expr = parseExpr()
    ensure(";")
    new ExprStmnt(expr)
  }

  def parseImport(): ImportStmnt = withSource {
    ensure("import")
    val what = delimited(".".asToken) {
      val result = unwrap(cur)
      next()
      result
    }

    val result = what.last match {
      case "*" => new ImportPkg(what.slice(0, what.length - 1))
      case _   => new ImportClass(new Typename(what))
    }

    ensure(";")
    result
  }

  def parseWhile(): WhileStmnt = withSource {
    ensure("while")
    ensure("(")
    val cond = parseExpr()
    ensure(")")
    val body = parseStmnt()

    new WhileStmnt(cond, body)
  }

  def parseIf(): IfStmnt = withSource {
    ensure("if")
    ensure("(")
    val cond = parseExpr()
    ensure(")")
    val then = parseStmnt()

    val otherwise = if (check("else")) {
      ensure("else")
      Some(parseStmnt())
    } else None

    new IfStmnt(cond, then, otherwise)
  }

  def parseFor(): ForStmnt = withSource {
    ensure("for")

    ensure("(")
    val first =
      if (!check(";"))
        Some(parseStmnt())
      else {
        ensure(";")
        None
      }

    val cond = if (!check(";")) {
      Some(parseExpr())
    } else None
    ensure(";")

    val after = if (!check(")")) {
      Some(parseExpr())
    } else None
    ensure(")")

    val body = parseStmnt()

    new ForStmnt(first, cond, after, body)
  }

  // Outermost expression parser
  def parseExpr(): Expression = withSource {
    parseExprAssign()
  }

  def parseExprAssign(): Expression = withSource {
    val lhs = parseExprPrec(0)

    if (check("=")) {
      next()
      new Assignment(lhs, parseExpr())
    } else lhs
  }

  // Parse left-associative binary operators with precent `level`
  def parseExprPrec(level: Int): Expression = withSource {
    // TODO: this is probably not a good use for withSource (ideally we want to
    // track the operator token)
    def parseNextPrec() =
      if (level + 1 < operators.length)
        parseExprPrec(level + 1)
      else parseUnaryExpr()

    var lhs = parseNextPrec()
    operators(level).foreach { case (sym, constructor) =>
      while (check(sym)) {
        next()
        // HACK: big ugly programming is here, but it's too much work to do
        // properly now
        if (sym == "instanceof") {
          lhs = new InstanceOf(lhs, qualifiedName())
        } else {
          val rhs = parseNextPrec()
          lhs = constructor()((lhs, rhs))
        }
      }
    }

    lhs
  }

  def parseUnaryExpr(): Expression = withSource {
    if (check("-")) {
      next()
      cur match {
          case IntLiteral(l) => {
            if ((-l).isValidInt) {
              next();
              new IntVal((-l).toInt)
            } else {
              throw new UnexpectedError("Invalid integer literal " + (-l), cur.from)
            }
          }
          case _ => new Neg(parseUnaryExpr())
      }
    } else if (check("!")) {
      next()
      new Not(parseUnaryExpr())
    } else parseCast()
  }

  def parseCast(): Expression = withSource {
    if (check("(")) {
      tokens.setBacktrace()
      try {
        next()
        val tname = qualifiedName()
        ensure(")")
        val value = parseUnaryExpr()

        val result = new Cast(tname, value)
        tokens.unsetBacktrace()
        return result
      } catch {
        case UnexpectedError(_, _) => tokens.backtrack()
      }
    }

    parsePostOp()
  }

  def parsePostOp(): Expression = withSource {
    var lhs = parseTerminal()
    while (check("(") || check("[") || check(".")) {
      if (check("(")) {
        lhs = new Call(lhs, parseArgs)
      } else if (check("[")) {
        next()
        val index = parseExpr()
        ensure("]")

        lhs = new Index(lhs, index)
      } else if (check(".")) {
        next()
        val member = new Id(unwrap(ensureIdentifier()))

        lhs = new Member(lhs, member)
      }
    }

    lhs
  }

  // Innermost expression parser
  def parseTerminal(): Expression = withSource {
    cur match {
      case IntLiteral(l) =>
        next()
        if (l.isValidInt) {
          new IntVal(l.toInt)
        } else {
          throw new UnexpectedError("Invalid integer literal " + l, cur.from)
        }
      case BoolLiteral(b) =>
        next()
        new BoolVal(b)

      case CharLiteral(c) =>
        next()
        new CharVal(c)

      case StringLiteral(s) =>
        next()
        new StringVal(s)

      case LParen() =>
        next()
        val result = parseExpr()
        ensure(")")
        result

      case ThisLiteral() =>
        next()
        new ThisVal()

      case SuperLiteral() =>
        next()
        new SuperVal()

      case NullLiteral() =>
        next()
        new NullVal()

      case Keyword("new") =>
        next()
        val tname =
          if (checkPrimitive()) {
            Seq(unwrap(ensurePrimitive()))
          } else delimited(".".asToken)(unwrap(ensureIdentifier()))

        if (check("(")) {
          // new Type()
          new NewType(new Typename(tname), parseArgs())
        } else if (check("[")) {
          // new Array[]
          next()
          val size = parseExpr()
          ensure("]")

          if (check("["))
            throw Expected("non-multi array creation")

          new NewArray(new Typename(tname, true), size)
        } else throw Expected("constructor arguments or array length")

      case Identifier(id) =>
        new Id(unwrap(ensureIdentifier))

      case _ => throw Expected("terminal value")
    }
  }
}

