import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

import juicy.source.analysis.AnalysisProbe
import juicy.source.analysis.AnalysisProbe.MethodReturnError
import juicy.source.analysis.AnalysisProbe.UnreachableError
import juicy.source.ast._
import juicy.source.parser.Parser
import juicy.source.tokenizer.TokenStream

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

  "Analysis probe" should "pass serial statements" in {
    reachable("""
      a;
      b;
      c;
      d;
      """)
  }

  it should "fail serial statements after a return" in {
    unreachable("""
      return;
      a;
      b;
      c;
      d;
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
      stmnt;
      """)
  }

  it should "try both sides of an if" in {
    reachable("""
      if (true) return;
      else;
      stmnt;
      """)
  }

  it should "consider the OR of an if's reachability" in {
    unreachable("""
      if (true) return;
      else return;
      stmnt;
      """)
  }

  it should "have stupid rules for if" in {
    reachable("""
      if (true) return;
      reachable;
      """)
  }

  it should "fail to get through for(;;);" in {
    unreachable("""
      for (;;);
      stmnt;
      """)
  }

  it should "fail to get through for(;true;);" in {
    unreachable("""
      for (;true;);
      stmnt;
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
      stmnt;
      """)
  }

  it should "fold over blocks (pass)" in {
    reachable("""
      { a; b; }
      { c; d; }
      stmnt;
      """)
  }

  it should "fold over blocks (fail)" in {
    unreachable("""
      { a; b; return; }
      { c; d; }
      stmnt;
      """)
    unreachable("""
      { a; b; }
      { c; d; return; }
      stmnt;
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

}
