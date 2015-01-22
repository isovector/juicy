import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

import juicy.source.ast._
import juicy.source.parser._
import juicy.source.resolver._
import juicy.source.tokenizer._
import juicy.utils.visitor.VisitError

class ResolverSpec extends FlatSpec with ShouldMatchers {
  def parse(sources: String*) = {
    val files = sources.toList.map { source =>
      new Parser(new TokenStream(source)).parseFile()
    }

    Resolver(files)
    files
  }

  def know(sources: String*) = {
    val files = sources.toList.map { source =>
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
      class A { %s A test(); }
      class B extends A { final A test(); }
      """)
  }

  it should "not allow rewriting return types" in {
    knowAB("A", "B", """
      class A { A test(); }
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
      class Same { }
      interface ISame { }
      class A implements %s {}
      """)
  }

  it should "explode on cyclic classes" in {
    intercept[KnowerError] {
      know("""
        class A extends B {}
        class B extends A {}
        """)
    }
  }
}
