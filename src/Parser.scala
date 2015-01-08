package juicy.source

import juicy.ast._
import juicy.ast.AST._
import juicy.source.tokenizer._
import juicy.source.tokenizer.Token._


class Parser(tokens: TokenStream) extends ParserUtils {
  def cur: Token = tokens.cur
  def next() = tokens.next()

  def operators = Seq(
    Map(
      "=" -> (Assignment.tupled _)
    ),
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

  def qualifiedName(): String = {
    delimited(".".asToken) {
      unwrap(ensureIdentifier())
    }.mkString(".")
  }

  // Coalesce subsequent modifier tokens into a bitfield
  def parseModifiers(): Modifiers.Value = {
    val mods = tokens.takeWhile{
        case Modifier(name) => true
        case _              => false
    }

    // TODO: check for proper order of modifiers
    (0 /: mods)((agg, token) => agg | Modifiers.parse(unwrap(token)) )
  }

  // Outermost parser for a file
  def parseFile(): Node = {
    // TODO: packages
    // TODO: imports

    val mods = parseModifiers()
    parseClass(mods)
  }

  def parseClass(mods: Modifiers.Value): ClassDefn = {
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
    val members = klein("}".asToken)(parseClassMember)

    // Separate members into fields and methods
    val (fields, methods) = members.partition { member =>
      member match {
        case (_: VarStmnt)   => true
        case (_: MethodDefn) => false
        case _ =>
          throw new Exception("Unexpected member in class: " + member.toString)
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
  def parseClassMember(): Node = {
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
      tname: String): VarStmnt = {
    val result = new VarStmnt(name, mods, tname, parseInitializer())
    ensure(";")
    result
  }

  def parseMethod(
      name: String,
      mods: Modifiers.Value,
      tname: String): MethodDefn = {
    ensure("(")

    // While we don't hit a `)`, parse args delimited by `,`
    val args = klein(")".asToken) {
      delimited(",".asToken) {
        val arg_tname = qualifiedName()
        val arg_name = unwrap(ensureIdentifier())
        val value = parseInitializer()
        new VarStmnt(arg_name, Modifiers.NONE, arg_tname, value)
      }
    }.flatMap(xs => xs) // run Seq(Seq()) to Seq()

    ensure(")")

    val body = parseBlock()
    new MethodDefn(name, mods, tname, args, body)
  }

  // Parse `{ Statement[] }`
  def parseBlock(): BlockStmnt = {
    ensure("{")
    val body = klein("}".asToken)(parseStmnt)
    ensure("}")

    new BlockStmnt(body)
  }

  def parseStmnt(): Statement = {
    if (check(";")) {
      new BlockStmnt(Seq())
    } else if (check("while")) {
      parseWhile()
    } else if (check("for")) {
      parseFor()
    } else if (check("if")) {
      parseIf()
    } else if (check("{")) {
      parseBlock()
    } else {
      // TODO
      throw new Exception("PROGRAMMING IS HARD. STATEMENTS NOT IMPLEMENTED")
    }
  }

  def parseWhile(): WhileStmnt = {
    ensure("while")
    ensure("(")
    val cond = parseExpr()
    ensure(")")
    val body = parseStmnt()

    new WhileStmnt(cond, body)
  }

  def parseIf(): IfStmnt = {
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

  def parseFor(): ForStmnt = {
    ensure("for")

    ensure("(")
    // TODO: first
    val first = None
    ensure(";")

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
  def parseExpr(): Expression = {
    parseExprPrec(0)
  }

  // Parse left-associative binary operators with precent `level`
  def parseExprPrec(level: Int): Expression = {
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

  def parseUnaryExpr(): Expression = {
    // TODO: unary expressions
    parseLiteral()
  }

  // Innermost expression parser
  def parseLiteral(): Expression = {
    // TODO: this only does integer literals =)
    cur match {
      case IntLiteral(i) =>
        next()
        new ConstIntExpr(i)

      case BoolLiteral(b) =>
        next()
        new ConstBoolExpr(b)

      case _ => throw new Exception("Expected literal, got " + cur.toString)
    }
  }
}

