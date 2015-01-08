import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

import juicy.ast.AST
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
    result.extnds should be  === Some("Parent")
    result.impls should be   === Seq("IA", "pkg.IB")
    result.fields should be  === Seq()
    result.methods should be === Seq()
  }


  it should "parse fullied qualified names" in {
    val parser = mkParser("java.lang.Object")
    parser.qualifiedName() should be === "java.lang.Object"
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
    uninit.tname should be === "bool"
    uninit.value should be === None

    val five = results(1)
    five.name should be  === "five"
    five.mods should be  === (STATIC | FINAL)
    five.tname should be === "int"
    five.value should be === Some(AST.ConstIntExpr(5))
  }


  it should "parse class methods" in {
    val parser = mkParser("""
      class Test {
        void simple() { }
        protected int add(int a, long b = 0) { }
      } """)
    val results = parser.parseClass(NONE).methods

    val simple = results(0)
    simple.name should be  === "simple"
    simple.mods should be  === NONE
    simple.tname should be === "void"
    simple.args should be === Seq()

    val add = results(1)
    add.name should be  === "add"
    add.mods should be  === PROTECTED
    add.tname should be === "int"

    val arg_a = add.args(0)
    arg_a.name should be === "a"
    arg_a.tname should be === "int"
    arg_a.value should be === None

    val arg_b = add.args(1)
    arg_b.name should be === "b"
    arg_b.tname should be === "long"
    arg_b.value should be === Some(AST.ConstIntExpr(0))
  }
}
