import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

import juicy.source.analysis.AnalysisProbe
import juicy.source.analysis.AnalysisProbe.MethodReturnError
import juicy.source.analysis.AnalysisProbe.UninitVarError
import juicy.source.analysis.AnalysisProbe.UnreachableError
import juicy.source.ast._
import juicy.source.parser.Parser
import juicy.source.scoper._
import juicy.source.tokenizer.TokenStream
import juicy.utils.visitor.VisitError

class AnalysisProbeSpec extends FlatSpec with ShouldMatchers {
  def reachable(src: String) = {
    val file =
      new Parser(
        new TokenStream("class A { void A() {" + src + "}}")).parseFile()
    AnalysisProbe(file)
  }

  def unreachable(src: String) = {
    intercept[UnreachableError] {
      reachable(src)
    }
  }

  def init(src: String) = reachable(src)

  def returns(src: String) = {
    val file =
      new Parser(
        new TokenStream("class A { Type A() {" + src + "}}")).parseFile()
    AnalysisProbe(file)
  }

  def noreturn(src: String) = {
    intercept[MethodReturnError] {
      returns(src)
    }
  }

  def probe(src: String) = {
    val file =
      new Parser(
        new TokenStream("class A {" + src + "}")).parseFile()

    file.classes.map(Hashtag360NoScoper(_))
    AnalysisProbe(file)
  }

  "Analysis probe" should "pass serial statements" in {
    reachable("""
      a();
      b();
      c();
      d();
      """)
  }

  it should "fail serial statements after a return" in {
    unreachable("""
      return;
      a();
      b();
      c();
      d();
      """)
  }

  it should "fail to get through while(true)" in {
    unreachable("""
      while (true);
      stmnt;
      """)
  }

  it should "get through while(true) with a return" in {
    reachable("""
      while (true) return;
      stmnt();
      """)
  }

  it should "try both sides of an if" in {
    reachable("""
      if (true) return;
      else;
      stmnt();
      """)
  }

  it should "consider the OR of an if's reachability" in {
    unreachable("""
      if (true) return;
      else return;
      stmnt();
      """)
  }

  it should "have stupid rules for if" in {
    reachable("""
      if (true) return;
      stmnt();
      """)
  }

  it should "fail to get through for(;;);" in {
    unreachable("""
      for (;;);
      stmnt();
      """)
  }

  it should "fail to get through for(;true;);" in {
    unreachable("""
      for (;true;);
      stmnt();
      """)
  }

  it should "pass through for(;;) return;" in {
    reachable("""
      for (;;) return;
      """)
  }

  it should "pass through for(;true;) return;" in {
    reachable("""
      for (;true;) return;
      """)
  }

  it should "pass for(;false;);" in {
    reachable("""
      for (;false;);
      stmnt();
      """)
  }

  it should "fold over blocks" in {
    reachable("""
      { a(); b(); }
      { c(); d(); }
      stmnt();
      """)
    unreachable("""
      { a(); b(); return; }
      { c(); d(); }
      stmnt();
      """)
    unreachable("""
      { a(); b(); }
      { c(); d(); return; }
      stmnt();
      """)
  }

  it should "ensure all code paths return" in {
    returns(
      """
      return new Type();
      """
      )

    returns(
      """
      return;
      """
      )
  }

  it should "fail non-returning code paths" in {
    noreturn(
      """
      """
      )
  }

  it should "fail non-returning branching code paths" in {
    noreturn(
      """
      if (true);
      else return;
      """
      )
  }

  it should "fail uninit var usage" in {
    intercept[VisitError] {
      init(
        """
        Type x = null;
        test(x);
        """
      )
    }
  }

  it should "fail uninit var usage with null assign" in {
    intercept[VisitError] {
      init(
        """
        Type x = null;
        x = null;
        test(x);
        """
      )
    }
  }

  it should "fail var init with self assign" in {
    intercept[UninitVarError] {
      init(
        """
        Type x = x;
        """
      )
    }
  }

  it should "fail uninit var usage with self assign" in {
    intercept[VisitError] {
      init(
        """
        Type x = null;
        x = x;
        test(x);
        """
      )
    }
  }

  it should "pass uninit var usage with non-null assign" in {
    init(
      """
      Type x = null;
      x = 5;
      test(x);
      """
      )
  }

  it should "require all code paths to non-null assign" in {
    init(
      """
      Type x = null;
      if (true)
        x = 5;
      else
        x = 5;
      test(x);
      """
    )

    intercept[VisitError] {
      init(
        """
        Type x = null;
        if (true)
          x = 5;
        else;
        test(x);
        """
      )
    }
  }

  it should "ensure consistent field initialization order" in {
    probe(
      """
      int x = 5;
      int y = x;
      int z = y;
      """
    )

    intercept[VisitError] {
      probe(
        """
        int x = y;
        int y = z;
        int z = 5;
        """
      )
    }
  }
}
