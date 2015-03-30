import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import scala.reflect.ClassTag

import juicy.source.ast._
import juicy.source.checker._
import juicy.source.disambiguator._
import juicy.source.PackageTree
import juicy.source.parser._
import juicy.source.resolver._
import juicy.source.scoper._
import juicy.source.tokenizer._
import juicy.utils.CompilerError
import juicy.utils.visitor._

object CheckerSpec {
  juicy.source.weeder.Weeder.debug.checkFileName = true

  val stdlib = Seq("""
    package java.lang;
    class Object {
      public Object() { }
      String toString() { return "fuckfuckfuck"; }
      boolean equals(Object o) { return false; }
    }
    """, """
    package java.lang;
    class String {
      public String() {}
      public static String valueOf (Object o) { return o.toString(); }
      public static String valueOf (boolean b) { return "false"; }
      public String whatever() { return String.valueOf(false); }
    }
    """,
    """
    package java.lang;
    class Integer {
        public Integer() {}
    }
    """,
    """
    package java.lang;
    class Byte {
      public Byte() {}
    }
    """,
    """
    package java.lang;
    class Boolean {
      public Boolean() {}
    }
    """,
    """
    package java.lang;
    class Character {
      public Character() {}
    }
    """,
    """
    package java.lang;
    class Short {
      public Short() {}
    }
    """)
  def check(sources: String*): Seq[FileNode] = {
    val srcs = sources.toList ++ stdlib
    val files = srcs.map { source =>
      new Parser(new TokenStream(source)).parseFile()
    }

    val pkgtree = Resolver(files)
    HardlyKnower(pkgtree)
    files.foreach(Hashtag360NoScoper(_))
    Disambiguator(files, pkgtree).map(Checker(_, pkgtree))
  }
}
class CheckerSpec extends FlatSpec with ShouldMatchers {
  "Checker" should "properly read static references" in {
    val nodes = CheckerSpec.check("""
    package foo;
    class Bar {
      public Bar() { }
      public String foo(String s) {
        return s.whatever();
      }
    }
    """)
  }
  "Checker" should "rewrite string concatenation" in {
    val nodes = CheckerSpec.check("""
    class Test{
      public Test() { }
      public String v() {
        return "1" + null;
      }
      public String w() {
        return "2" + 3;
      }
      public String x() {
        return (Test)null + "z";
      }
      public String y() {
        return (byte)3 + ("q" + "s");
      }
      public String z() {
        return true + (String)null;
      }
    }
    """)
    def retExpr(ind: Int) = nodes(0).classes(0).methods(ind).body.get.children(0).children(0)
    def verifyConcat[T1 <: Expression : ClassTag, T2 <: Expression : ClassTag](e: Visitable) = {
      val lcls = implicitly[ClassTag[T1]].runtimeClass
      val rcls = implicitly[ClassTag[T2]].runtimeClass
      val expr = e match {
         case StringConcat(l, r) if lcls.isInstance(l) && rcls.isInstance(r) => Some(e)
         case _ => None
      }
      expr should be === Some(e)
    }
    verifyConcat[StrToStr, StrToStr](retExpr(1))
    verifyConcat[StrToStr, IntToStr](retExpr(2))
    verifyConcat[RefToStr, StrToStr](retExpr(3))
    verifyConcat[ByteToStr, StrToStr](retExpr(4))
    verifyConcat[BoolToStr, RefToStr](retExpr(5))
  }
}
