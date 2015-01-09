import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

import juicy.source._
import juicy.source.tokenizer._

class WeederSpec extends FlatSpec with ShouldMatchers {
  import juicy.source.tokenizer.Token._

  def mkParser(source: String) = new Parser(new TokenStream(source))

  "Weeder" should "fail abstract and final classes" in {
    val parser = mkParser("""
      abstract final class Fail { }
      final class Pass { }
      abstract class Pass { }
      """)

    // TODO: this will fail when we get a proper parseFile()
    Weeder(parser.parseFile()) should be === false
    Weeder(parser.parseFile()) should be === true
    Weeder(parser.parseFile()) should be === true
  }

  it should "fail abstract method bodies" in {
    val parser = mkParser("""
      class Class {
        abstract bool fail() { }
      }

      class Class {
        abstract bool pass();
      }
      """)

    // TODO: this will fail when we get a proper parseFile()
    Weeder(parser.parseFile()) should be === false
    Weeder(parser.parseFile()) should be === true
  }

  it should "fail static final abstract methods" in {
    val parser = mkParser("""
      class Class {
        abstract static bool fail();
      }

      class Class {
        abstract final bool fail();
      }

      class Class {
        abstract bool pass();
      }
      """)

    // TODO: this will fail when we get a proper parseFile()
    Weeder(parser.parseFile()) should be === false
    Weeder(parser.parseFile()) should be === false
    Weeder(parser.parseFile()) should be === true
  }

  it should "fail static final methods" in {
    val parser = mkParser("""
      class Class {
        static final bool fail();
      }

      class Class {
        static bool pass();
        final bool pass();
      }
      """)

    // TODO: this will fail when we get a proper parseFile()
    Weeder(parser.parseFile()) should be === false
    Weeder(parser.parseFile()) should be === true
  }

  it should "ensure native methods be static" in {
    val parser = mkParser("""
      class Class {
        native bool fail();
      }

      class Class {
        static native bool pass();
      }
      """)

    // TODO: this will fail when we get a proper parseFile()
    Weeder(parser.parseFile()) should be === false
    Weeder(parser.parseFile()) should be === true
  }

  it should "only allow void as a method type" in {
    val parser = mkParser("""
      class Class {
        bool fail(void a);
      }

      class Class {
        bool fail() {
          void a;
        }
      }

      class Class {
        void pass() { }
      }
      """)

    // TODO: this will fail when we get a proper parseFile()
    Weeder(parser.parseFile()) should be === false
    Weeder(parser.parseFile()) should be === false
    Weeder(parser.parseFile()) should be === true
  }

  it should "not allow final fields" in {
    val parser = mkParser("""
      class Class {
        final bool fail;
      }

      class Class {
        bool pass;
      }
      """)

    // TODO: this will fail when we get a proper parseFile()
    Weeder(parser.parseFile()) should be === false
    Weeder(parser.parseFile()) should be === true
  }

  it should "not allow array implementations or extends" in {
    val parser = mkParser("""
      class fail extends object[] { }
      class fail implements object[] { }
      class pass extends object implements object {}
      """)

    // TODO: this will fail when we get a proper parseFile()
    Weeder(parser.parseFile()) should be === false
    Weeder(parser.parseFile()) should be === false
    Weeder(parser.parseFile()) should be === true
  }
}
