import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

import juicy.ast._
import juicy.ast.Modifiers._
import juicy.source._
import juicy.source.tokenizer._

class ParserSpec extends FlatSpec with ShouldMatchers {
  import juicy.source.tokenizer.Token._

  def mkParser(source: String) = new Parser(new TokenStream(source))

  "Parser" should "parse empty classes" in {
    val parser = mkParser("class Basic { }")
    val result = parser.parseClass(NONE)

    result.name should be    === "Basic"
    result.mods should be    === NONE
    result.extnds should be  === None
    result.impls should be   === Seq()
    result.fields should be  === Seq()
    result.methods should be === Seq()
  }


  it should "parse extends and implements" in {
    val parser = mkParser(
      "class Child extends Parent implements IA, pkg.IB { }")
    val result = parser.parseClass(NONE)

    result.name should be    === "Child"
    result.mods should be    === NONE
    result.extnds should be  === Some(new Typename("Parent"))
    result.impls should be   === Seq("IA", "pkg.IB").map(Typename.tupled(_, false))
    result.fields should be  === Seq()
    result.methods should be === Seq()
  }


  it should "parse fullied qualified names" in {
    val parser = mkParser("java.lang.Object")
    parser.qualifiedName().toString should be === "java.lang.Object"
  }


  it should "parse modifiers" in {
    val parser = mkParser("public static final abstract")
    parser.parseModifiers() should be === (PUBLIC | STATIC | FINAL | ABSTRACT)
  }


  it should "parse class fields" in {
    val parser = mkParser("""
      class Test {
        public bool uninit;
        static final int five = 5;
      } """)
    val results = parser.parseClass(NONE).fields

    val uninit = results(0)
    uninit.name should be  === "uninit"
    uninit.mods should be  === PUBLIC
    uninit.tname.toString should be === "bool"
    uninit.value should be === None

    val five = results(1)
    five.name should be  === "five"
    five.mods should be  === (STATIC | FINAL)
    five.tname.toString should be === "int"
    five.value should be === Some(AST.ConstIntExpr(5))
  }


  it should "parse class methods" in {
    val parser = mkParser("""
      class Test {
        void simple();
        protected int add(int a, long b = 0) { }
      } """)
    val results = parser.parseClass(NONE).methods

    val simple = results(0)
    simple.name should be  === "simple"
    simple.mods should be  === NONE
    simple.tname.toString should be === "void"
    simple.params should be === Seq()
    simple.body should be === None

    val add = results(1)
    add.name should be  === "add"
    add.mods should be  === PROTECTED
    add.tname.toString should be === "int"
    add.body should be === Some(AST.BlockStmnt(Seq()))

    val arg_a = add.params(0)
    arg_a.name should be === "a"
    arg_a.tname.toString should be === "int"
    arg_a.value should be === None

    val arg_b = add.params(1)
    arg_b.name should be === "b"
    arg_b.tname.toString should be === "long"
    arg_b.value should be === Some(AST.ConstIntExpr(0))
  }

  it should "parse if with and without else" in {
    val parser = mkParser("""
      if (1)
        if (2) { }
        else { }
       """)
    val result = parser.parseIf()

    result.cond should be === AST.ConstIntExpr(1)
    result.otherwise should be === None

    val inner = result.then.asInstanceOf[AST.IfStmnt]
    inner.cond should be === AST.ConstIntExpr(2)
    inner.then should be === AST.BlockStmnt(Seq())
    inner.otherwise should be === Some(AST.BlockStmnt(Seq()))
  }

  it should "parse while loops" in {
    val parser = mkParser("while (true);")
    val result = parser.parseWhile()

    result.cond should be === AST.ConstBoolExpr(true)
    result.body should be === AST.BlockStmnt(Seq())
  }

  it should "parse janky for loops" in {
    val parser = mkParser("for (; false;);")
    val result = parser.parseFor()

    result.first should be === None
    result.cond should be === Some(AST.ConstBoolExpr(false))
    result.after should be === None
    result.body should be === AST.BlockStmnt(Seq())
  }

  it should "parse non-janky for loops" in {
    val parser = mkParser("for (int i = 0; i < 5;);")
    val result = parser.parseFor()

    result.first should be ===
      Some(AST.VarStmnt("i", NONE, Typename("int"), Some(AST.ConstIntExpr(0))))
    result.cond should be ===
      Some(AST.LThan(AST.Id("i"), AST.ConstIntExpr(5)))
    result.after should be === None
    result.body should be === AST.BlockStmnt(Seq())
  }

  it should "parse binary expressions with precedence" in {
    def coerce(expr: Expression) = expr.asInstanceOf[BinaryOperator]

    val parser = mkParser("1 + 2 * 5 = true")
    val result = coerce(parser.parseExpr())

    result.rhs should be === AST.ConstBoolExpr(true)
    coerce(result.lhs).lhs should be === AST.ConstIntExpr(1)

    val mul = coerce(coerce(result.lhs).rhs)
    mul.lhs should be === AST.ConstIntExpr(2)
    mul.rhs should be === AST.ConstIntExpr(5)
  }

  it should "right-associate assignments" in {
    val parser = mkParser("a = b = 5")
    val result = parser.parseExpr()

    result should be ===
      AST.Assignment(AST.Id("a"),
        AST.Assignment(AST.Id("b"), AST.ConstIntExpr(5)))
  }


  it should "parse variable declarations" in {
    val parser = mkParser("a.b.c.d var1; java.lang.Object var2 = 5;")
    val var1 = parser.parseStmnt().asInstanceOf[AST.VarStmnt]
    val var2 = parser.parseStmnt().asInstanceOf[AST.VarStmnt]

    var1.name should be === "var1"
    var1.mods should be === NONE
    var1.tname should be === new Typename("a.b.c.d")
    var1.value should be === None

    var2.name should be === "var2"
    var2.mods should be === NONE
    var2.tname.toString should be === "java.lang.Object"
    var2.value should be === Some(AST.ConstIntExpr(5))
  }

  it should "parse variable assignment statements" in {
    def coerce(expr: Expression) = expr.asInstanceOf[AST.Member]

    val parser = mkParser("a.b.c = 1337;")
    val expr = parser.parseStmnt().asInstanceOf[AST.ExprStmnt].expr
    val assign = expr.asInstanceOf[AST.Assignment]

    assign.rhs should be === AST.ConstIntExpr(1337)
    coerce(assign.lhs).rhs should be === AST.Id("c")
    coerce(coerce(assign.lhs).lhs).rhs should be === AST.Id("b")
    coerce(coerce(assign.lhs).lhs).lhs should be === AST.Id("a")
  }

  it should "parse array declarations" in {
    val parser = mkParser("java.lang.String [] m;")
    val var1 = parser.parseStmnt().asInstanceOf[AST.VarStmnt]
    var1.name should be === "m"
    var1.mods should be === NONE
    var1.tname.toString should be === "java.lang.String []"
    var1.value should be === None
  }

  it should "allow method call statements" in {
    def coerce(expr: Expression) = expr.asInstanceOf[AST.Member]

    val parser = mkParser("object.method(1, 2, 3);")
    val expr = parser.parseStmnt().asInstanceOf[AST.ExprStmnt].expr
    val call = expr.asInstanceOf[AST.Call]

    call.method should be === AST.Member(AST.Id("object"), AST.Id("method"))
    call.args should have length 3
    call.args(0) should be === AST.ConstIntExpr(1)
    call.args(1) should be === AST.ConstIntExpr(2)
    call.args(2) should be === AST.ConstIntExpr(3)
  }

  it should "parse unary operators" in {
    val parser = mkParser("-!!a")
    val result = parser.parseExpr()

    result should be ===
      AST.Sub(AST.ConstIntExpr(0), AST.Not(AST.Not(AST.Id("a"))))
  }

  it should "parse new arrays and types" in {
    val parser = mkParser("new obj[new test(cool)]")
    val result = parser.parseExpr()

    result should be ===
      AST.NewArray(new Typename("obj", true),
        AST.NewType(new Typename("test"), Seq(AST.Id("cool"))))
  }

  it should "parse postfix operators" in {
    val parser = mkParser("a[b()].c")
    val result = parser.parseExpr()

    result should be ===
      AST.Member(
        AST.Index(AST.Id("a"), AST.Call(AST.Id("b"), Seq())),
        AST.Id("c"))
  }
}
