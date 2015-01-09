package juicy.source

import juicy.ast._
import juicy.ast.AST._
import juicy.source.tokenizer._
import juicy.source.tokenizer.Token._

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
      "instanceof" -> (InstanceOf.tupled _)
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

  def qualifiedName(): Typename = {
    new Typename(delimited(".".asToken) {
      unwrap(ensureIdentifier())
    }.mkString("."), consumeArray())
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

    (0 /: mods)((agg, token) => agg | Modifiers.parse(unwrap(token)) )
  }

  // Outermost parser for a file
  def parseFile(): Node = {
    // TODO: packages
    // TODO: imports

    val mods = parseModifiers()
    parseClass(mods)
  }

  def parseClass(mods: Modifiers.Value): ClassDefn = withSource {
    ensure("class")

    val name = unwrap(cur) // UNSURE: should this be a qualifiedName?
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
    val members = kleene("}".asToken)(parseClassMember)

    // Separate members into fields and methods
    val (fields, methods) = members.partition { member =>
      member match {
        case (_: VarStmnt)   => true
        case (_: MethodDefn) => false
        case _ => throw Expected("field or method")
      }
    }
    ensure("}")

    new ClassDefn(
      name,
      mods,
      extnds,
      impls,
      fields.map(_.asInstanceOf[VarStmnt]),
      methods.map(_.asInstanceOf[MethodDefn]))
  }

  // Parse modifiers, types and names, and then delegate parsing to methods
  // or fields.
  def parseClassMember(): Node = withSource {
    val mods = parseModifiers()
    val tname = qualifiedName()
    val name = unwrap(ensureIdentifier())

    if (check("(")) {
      parseMethod(name, mods, tname)
    } else {
      parseField(name, mods, tname)
    }
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
    } else if (checkIdentifier()) {
      // Potentially parse a vardecl
      val possible_tname = delimited(".".asToken)(unwrap(ensureIdentifier()))

      if (checkIdentifier()) {
        // Variable definition
        val tname = possible_tname.mkString(".")
        val isArray = consumeArray()
        val name = unwrap(ensureIdentifier())
        val value = parseInitializer()
        ensure(";")

        new VarStmnt(name, Modifiers.NONE, new Typename(tname, isArray), value)
      } else if (check("=")) {
        // Variable assignment
        val lhs = foldMemberAccess(possible_tname)
        val value = parseInitializer().get
        ensure(";")

        new ExprStmnt(new Assignment(lhs, value))
      } else if (check("(")) {
        val method = foldMemberAccess(possible_tname)
        val args = parseArgs()
        ensure(";")

        new ExprStmnt(new Call(method, args))
      } else {
        throw new Exception("PROGRAMMING IS HARD. STATEMENTS NOT IMPLEMENTED")
      }
    } else {
      throw new Exception("PROGRAMMING IS HARD. STATEMENTS NOT IMPLEMENTED")
    }
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
        val rhs = parseNextPrec()
        lhs = constructor()((lhs, rhs))
      }
    }

    lhs
  }

  def parseUnaryExpr(): Expression = withSource {
    if (check("-")) {
      next()
      new Sub(new IntVal(0), parseUnaryExpr())
    } else if (check("!")) {
      next()
      new Not(parseUnaryExpr())
    } else if (check("new")) {
      next()
      val tname =
        delimited(".".asToken)(unwrap(ensureIdentifier())).mkString(".")

      if (check("(")) {
        // new Type()
        new NewType(new Typename(tname), parseArgs())
      } else if (check("[")) {
        // new Array[]
        next()
        val size = parseExpr()
        ensure("]")

        new NewArray(new Typename(tname, true), size)
      } else throw Expected("constructor arguments or array length")
    } else parseCast()
  }

  def parseCast(): Expression = withSource {
    if (check("(")) {
      tokens.setBacktrace()
      try {
        next()
        val tname = qualifiedName()
        ensure(")")
        val value = parseCast()

        val result = new Cast(tname, value)
        tokens.unsetBacktrace()
        return result
      } catch {
        case _: Exception => tokens.backtrack()
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
      case IntLiteral(i) =>
        next()
        new IntVal(i)

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

      // TODO: this and null

      case Identifier(id) =>
        new Id(unwrap(ensureIdentifier))

      case _ => throw Expected("terminal value")
    }
  }
}

