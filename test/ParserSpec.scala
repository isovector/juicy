import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

import juicy.source.ast._
import juicy.source.ast.Modifiers._
import juicy.source.parser._
import juicy.source.tokenizer._

class ParserSpec extends FlatSpec with ShouldMatchers {
  import juicy.source.tokenizer.Token._

  def mkParser(source: String) = new Parser(new TokenStream(source))
  def typename(name: String, isArray: Boolean = false) =
    new Typename(name.split("\\."), isArray)

  "Parser" should "parse empty classes" in {
    val parser = mkParser("class Basic { }")
    val result = parser.parseClass(NONE)

    result.name should be    === "Basic"
    result.mods should be    === NONE
    result.extnds should be  === Seq(typename("java.lang.Object"))
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
    result.extnds should be  === Seq(typename("Parent"))
    result.impls should be   ===
      Seq(Seq("IA"), Seq("pkg", "IB")).map(Typename.tupled(_, false))
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
    five.value should be === Some(IntVal(5))
  }

  it should "parse class methods" in {
    val parser = mkParser("""
      class Test {
        void simple();
        protected int add(int a, int b) { }
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
    add.body should be === Some(BlockStmnt(Seq()))

    val arg_a = add.params(0)
    arg_a.name should be === "a"
    arg_a.tname.toString should be === "int"
    arg_a.value should be === None

    val arg_b = add.params(1)
    arg_b.name should be === "b"
    arg_b.tname.toString should be === "int"
    arg_b.value should be === None
  }

  it should "parse if with and without else" in {
    val parser = mkParser("""
      if (1)
        if (2) { }
        else { }
       """)
    val result = parser.parseIf()

    result.cond should be === IntVal(1)
    result.otherwise should be === None

    val inner = result.then.asInstanceOf[BlockStmnt].children(0).asInstanceOf[IfStmnt]
    inner.cond should be === IntVal(2)
    inner.then should be === BlockStmnt(Seq())
    inner.otherwise should be === Some(BlockStmnt(Seq()))
  }

  it should "parse while loops" in {
    val parser = mkParser("while (true);")
    val result = parser.parseWhile()

    result.cond should be === BoolVal(true)
    result.body should be === BlockStmnt(Seq())
  }

  it should "parse janky for loops" in {
    val parser = mkParser("for (; false;);")
    val result = parser.parseFor()

    result.first should be === None
    result.cond should be === Some(BoolVal(false))
    result.after should be === None
    result.body should be === BlockStmnt(Seq())
  }

  it should "parse non-janky for loops" in {
    val parser = mkParser("for (int i = 0; i < 5;);")
    val result = parser.parseFor()

    result.first should be ===
      Some(VarStmnt("i", NONE, Typename(Seq("int")), Some(IntVal(0))))
    result.cond should be ===
      Some(LThan(Id("i"), IntVal(5)))
    result.after should be === None
    result.body should be === BlockStmnt(Seq())
  }

  it should "parse non-empty return statements" in {
    val parser = mkParser("return 5;")
    parser.parseStmnt() should be === ReturnStmnt(Some(IntVal(5)))
  }

  it should "parse empty return statements" in {
    val parser = mkParser("return;")
    parser.parseStmnt() should be === ReturnStmnt(None)
  }

  it should "parse binary expressions with precedence" in {
    def coerce(expr: Expression) = expr.asInstanceOf[BinOp]

    val parser = mkParser("1 + 2 * 5 = true")
    parser.parseExpr() should be ===
      Assignment(
        Assignee(
          Add(
            IntVal(1),
            Mul(IntVal(2), IntVal(5))
          )
        ),
        BoolVal(true)
      )
  }

  it should "right-associate assignments" in {
    val parser = mkParser("a = b = 5")
    val result = parser.parseExpr()

    result should be ===
      Assignment(Assignee(Id("a")),
        Assignment(Assignee(Id("b")), IntVal(5)))
  }


  it should "parse variable declarations" in {
    val parser = mkParser("java.lang.Object var2 = 5;")
    val var1 = parser.parseStmnt().asInstanceOf[VarStmnt]

    var1.name should be === "var2"
    var1.mods should be === NONE
    var1.tname.toString should be === "java.lang.Object"
    var1.value should be === Some(IntVal(5))
  }

  it should "parse variable assignment statements" in {
    def coerce(expr: Expression) = expr.asInstanceOf[Member]

    val parser = mkParser("a.b.c = 1337;")
    parser.parseStmnt() should be ===
      ExprStmnt(
        Assignment(
          Assignee(
            Member(
              Member(Id("a"), Id("b")),
              Id("c")
            )
          ),
          IntVal(1337)
        )
      )
  }

  it should "parse array declarations" in {
    val parser = mkParser("java.lang.String [] m = null;")
    val var1 = parser.parseStmnt().asInstanceOf[VarStmnt]
    var1.name should be === "m"
    var1.mods should be === NONE
    var1.tname.toString should be === "java.lang.String []"
    var1.value should be === Some(NullVal())
  }

  it should "have null, super and this literals" in {
    val parser = mkParser("this + super / null")
    parser.parseExpr() should be ===
      Add(ThisVal(), Div(SuperVal(), NullVal()))
  }

  it should "allow method call statements" in {
    def coerce(expr: Expression) = expr.asInstanceOf[Member]

    val parser = mkParser("object.method(1, 2, 3);")
    val expr = parser.parseStmnt().asInstanceOf[ExprStmnt].expr
    val call = expr.asInstanceOf[Call]

    call.method should be === Callee(Member(Id("object"), Id("method")))
    call.args should have length 3
    call.args(0) should be === IntVal(1)
    call.args(1) should be === IntVal(2)
    call.args(2) should be === IntVal(3)
  }

  it should "parse unary operators" in {
    val parser = mkParser("-!!a")
    val result = parser.parseExpr()

    result should be ===
      Neg(Not(Not(Id("a"))))
  }

  it should "parse new arrays and types" in {
    val parser = mkParser("new obj[new test(cool)]")
    val result = parser.parseExpr()

    result should be ===
      NewArray(typename("obj", true),
        NewType(typename("test"), Seq(Id("cool"))))
  }

  it should "parse postfix operators" in {
    val parser = mkParser("a[b()].c")
    val result = parser.parseExpr()

    result should be ===
      Member(
        Index(Id("a"), Call(Callee(Id("b")), Seq())),
        Id("c"))
  }

  it should "parse instances of" in {
    val parser = mkParser("""
      5 instanceof boolean
      "test" instanceof void
      a instanceof java.Reference[]
      true instanceof boolean instanceof boolean
      """)

    parser.parseExpr() should be ===
      InstanceOf(IntVal(5), typename("boolean"))
    parser.parseExpr() should be ===
      InstanceOf(StringVal("test"), typename("void"))
    parser.parseExpr() should be ===
      InstanceOf(Id("a"), typename("java.Reference", true))
    parser.parseExpr() should be ===
      InstanceOf(
        InstanceOf(BoolVal(true), typename("boolean")),
        typename("boolean"))
  }

  it should "parse parenthesized expressions" in {
    val parser = mkParser("((a))")
    val result = parser.parseExpr()

    result should be === Id("a")
  }

  it should "parse casts" in {
    val parser = mkParser("(int)(bool[])(a)")
    val result = parser.parseExpr()

    result should be ===
      Cast(typename("int"),
        Cast(typename("bool", true), Id("a")))
  }

  it should "parse constructors" in {
    val parser = mkParser("class A { public A(); static A(int B); }")
    val result = parser.parseFile()

    val classes = result.classes
    classes(0).methods.filter(_.isCxr) should be ===
      Seq(
        MethodDefn(".ctor.A", PUBLIC, true, typename("A"), Seq(), None),
        MethodDefn(
          ".ctor.A",
          STATIC,
          true,
          typename("A"),
          Seq(
            VarStmnt("B", NONE, typename("int"), None)),
          None))
  }

  it should "fail to parse multi-array creation" in {
    val parser = mkParser("new int[5][6]")

    intercept[UnexpectedError] {
      parser.parseExpr()
    }
  }

  it should "parse multiple extends" in {
    ClassDefn.suspendUniqueness {
    mkParser("""
      interface A extends IA, IB { }
      """)
    .parseFile().classes(0) should be ===
      ClassDefn(
        "A",
        Seq(".DEFAULT"),
        NONE,
        Seq(typename("IA"), typename("IB")),
        Seq(), Seq(), Seq(), true)
    }
  }

  it should "parse files" in {
    val parser = mkParser("""
      package look.mom;
      import a;
      import b.*;

      class Hello { }
      public interface Jello { }
      """)

    ClassDefn.suspendUniqueness {
    val result = parser.parseFile()

    result should be ===
      FileNode(
        Seq("look", "mom"),
        Seq(
          new ImportClass(typename("a")),
          new ImportPkg(Seq("b"))),
        Seq(
          new ClassDefn("Hello", Seq("look", "mom"), NONE, Seq(typename("java.lang.Object")), Seq(), Seq(), Seq()),
          new ClassDefn(
            "Jello", Seq("look", "mom"), PUBLIC, Seq(typename("java.lang.Object")), Seq(), Seq(), Seq(), true)))
    }
  }

  it should "not parse multiple stars in imports" in {
    val parser = mkParser("""
      import b.*.*.*;
      """)

    intercept[UnexpectedError] {
      parser.parseFile()
    }
  }

  it should "not fail on easy java programs" in {
    val parser = mkParser("""
      class BubbleSort {
        public static void main(String []args) {
          Scanner in = new Scanner(System.in);

          System.out.println("Input number of integers to sort");
          n = in.nextInt();

          int[] array = new int[n];

          System.out.println("Enter " + n + " integers");

          for (c = 0; c < n; c = c + 1)
            array[c] = in.nextInt();

            for (c = 0; c < ( n - 1 ); c = c + 1) {
              for (d = 0; d < n - c - 1; d = d + 1) {
                if (array[d] > array[d+1]) /* For descending order use < */
                {
                  swap       = array[d];
                  array[d]   = array[d+1];
                  array[d+1] = swap;
                }
              }
            }

            System.out.println("Sorted list of numbers");

            for (c = 0; c < n; c = c + 1)
              System.out.println(array[c]);
        }
      }
      """)

    parser.parseFile()
  }

  it should "fail on duplicate modifiers" in {
    val parser = mkParser("""
      class Failure {
        public public int x;
      }
      """)

    intercept[UnexpectedError] {
      parser.parseExpr()
    }
  }

  it should "parse things from marmotest that it isn't" in {
    mkParser("return o1 - o2 + 123;").parseStmnt()
  }
}
