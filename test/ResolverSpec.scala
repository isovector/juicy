import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

import juicy.source.ast._
import juicy.source.parser._
import juicy.source.resolver._
import juicy.source.tokenizer._
import juicy.utils.CompilerError
import juicy.utils.visitor.VisitError

object ResolverSpec {
  val stdlib = Seq("""
    package java.lang;
    class Object {
      public Object() { }
      String toString() { }
      boolean equals(Object o) { }
    }
    """, """
    package java.lang;
    class String {
    }
    """)
}

class ResolverSpec extends FlatSpec with ShouldMatchers {
  def parse(sources: String*) = {
    val srcs = sources.toList ++ ResolverSpec.stdlib
    val files = srcs.map { source =>
      new Parser(new TokenStream(source)).parseFile()
    }

    Resolver(files)
    files
  }

  def know(sources: String*) = {
    val srcs = sources.toList ++ ResolverSpec.stdlib
    val files = srcs.map { source =>
      new Parser(new TokenStream(source)).parseFile()
    }

    HardlyKnower(Resolver(files))
    files
  }


  "Resolver" should "resolve self-referential types" in {
    val ast = parse("""
      class A {
        public A a;
      }
      """)

    ast(0).classes(0).fields(0).tname.resolved should be ===
      Some(ast(0).classes(0))
  }

  it should "consider the default package unique" in {
    intercept[VisitError] {
      parse("""
        class A {
          B b;
        }
        """, """
        package BB;
        class B {
          A a;
        }
        """)
    }
  }

  it should "resolve between two files" in {
    val ast = parse("""
      class A {
        B b;
      }
      """, """
      class B {
        A a;
      }
      """)

    ast(1).classes(0).fields(0).tname.resolved should be ===
      Some(ast(0).classes(0))
    ast(0).classes(0).fields(0).tname.resolved should be ===
      Some(ast(1).classes(0))
  }

  it should "resolve types between packages and imports" in {
    val ast = parse("""
      package pkg1;
      import pkg2.B;

      class A {
        B b;
      }
      """, """
      package pkg2;

      class B {
        pkg1.A a;
      }
      """)

    ast(1).classes(0).fields(0).tname.resolved should be ===
      Some(ast(0).classes(0))
    ast(0).classes(0).fields(0).tname.resolved should be ===
      Some(ast(1).classes(0))
  }

  it should "resolve types in the same package" in {
    val ast = parse("""
      package pkg;

      class A {
        B b;
      }
      """, """
      package pkg;

      class B {
        A a;
      }
      """)

    ast(1).classes(0).fields(0).tname.resolved should be ===
      Some(ast(0).classes(0))
    ast(0).classes(0).fields(0).tname.resolved should be ===
      Some(ast(1).classes(0))
  }

  it should "resolve import paths" in {
    val ast = parse("""
      package big.path.name;

      class A { }
      """, """
      import big.path.name.*;
      class B {
        A a;
      }
      """)

    ast(1).classes(0).fields(0).tname.resolved should be ===
      Some(ast(0).classes(0))
  }

  it should "fail to resolve unknown types" in {
    intercept[VisitError] {
      parse("""
        class A {
          Unknown a;
        }
        """)
    }
  }

  it should "fail to find unknown package" in {
    intercept[Resolver.UnknownPackageError] {
      parse("""
        import unknown.*;
        class A { }
        """)
    }
  }

  it should "resolve primitives" in {
    val ast = parse("""
      class A {
        int I = 5;
        byte B = 1;
        boolean BB = true;
        char C = 'c';
        short S = 3;
      }
      """)(0)

    def resolvedType(which: Int) =
      ast.classes(0).fields(which).tname.resolved.get

    resolvedType(0).name should be === "int"
    resolvedType(1).name should be === "byte"
    resolvedType(2).name should be === "boolean"
    resolvedType(3).name should be === "char"
    resolvedType(4).name should be === "short"
  }

  it should "fail prefix overlaps" in {
    intercept[CompilerError] {
      parse("""
        package java;
        class lang {
        }
        """)
    }
  }

  // ---------- BEGIN KNOWER TESTS -------------

  // Compile a bit of code with a '%s' marker twice, once with it pass subbed,
  // and once with fail
  def knowAB(pass: String, fail: String, f: String) = {
    intercept[VisitError](know(f.format(fail)))
    know(f.format(pass))
  }

  "Knower" should "not allow non-abstract classes with abstract methods" in {
    knowAB("", "abstract", """
      class A { %s A test(); }
      """)
  }

  it should "not allow hiding of final methods" in {
    knowAB("", "final", """
      class A { public A() {} %s A test(); }
      class B extends A { final A test(); }
      """)
  }

  it should "not allow rewriting return types" in {
    knowAB("A", "B", """
      class A { public A() {} A test(); }
      class B extends A { %s test(); }
      """)
  }

  it should "not allow non-unique signatures" in {
    knowAB("A", "Same", """
      class Same { }
      class A {
        A test(Same name);
        A test(%s name);
      }
      """)
  }

  it should "not allow implementing classes" in {
    knowAB("ISame", "Same", """
      class Same { public Same() { } }
      interface ISame { }
      class A implements %s {}
      """)
  }

  it should "explode on cyclic classes" in {
    intercept[KnowerError] {
      know("""
        class A extends B { public A() { }}
        class B extends A { public B() { }}
        """)
    }
  }

  it should "ensure interfaces are enforced" in {
    knowAB("boolean", "int", """
      interface I {
        void method(%s a);
      }
      class A implements I {
        void method(boolean a) { }
      }
      """)

    knowAB("boolean", "int", """
      interface I {
        %s method();
      }
      class A implements I {
        boolean method() { }
      }
      """)
  }

  it should "enforce multiplicity 1 of implementing interfaces" in {
    knowAB("IB", "IA", """
      interface IA { }
      interface IB { }
      class AB implements IA, %s { }
      """)
  }

  it should "enforce multiplicity 1 of extending interfaces" in {
    knowAB("IB", "IA", """
      interface IA { }
      interface IB { }
      interface IAB extends IA, %s { }
      """)
  }
}
