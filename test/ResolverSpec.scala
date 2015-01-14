import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

import juicy.source.ast.AST._
import juicy.source.parser._
import juicy.source.resolver._
import juicy.source.tokenizer._

class ResolverSpec extends FlatSpec with ShouldMatchers {
  def parse(sources: String*) = {
    val files = sources.toList.map { source =>
      new Parser(new TokenStream(source)).parseFile()
    }

    Resolver(files)
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
      package big.long.path.name;

      class A { }
      """, """
      import big.long.*;
      class B {
        path.name.A a;
      }
      """)

    ast(1).classes(0).fields(0).tname.resolved should be ===
      Some(ast(0).classes(0))
  }
}

