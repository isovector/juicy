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

    val classes = parser.parseFile().classes

    Weeder(classes(0)) should be === false
    Weeder(classes(1)) should be === true
    Weeder(classes(2)) should be === true
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

    val classes = parser.parseFile().classes

    Weeder(classes(0)) should be === false
    Weeder(classes(1)) should be === true
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

    val classes = parser.parseFile().classes

    Weeder(classes(0)) should be === false
    Weeder(classes(1)) should be === false
    Weeder(classes(2)) should be === true
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

    val classes = parser.parseFile().classes

    Weeder(classes(0)) should be === false
    Weeder(classes(1)) should be === true
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

    val classes = parser.parseFile().classes

    Weeder(classes(0)) should be === false
    Weeder(classes(1)) should be === true
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

    val classes = parser.parseFile().classes

    Weeder(classes(0)) should be === false
    Weeder(classes(1)) should be === false
    Weeder(classes(2)) should be === true
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

    val classes = parser.parseFile().classes

    Weeder(classes(0)) should be === false
    Weeder(classes(1)) should be === true
  }

  it should "not allow array implementations or extends" in {
    val parser = mkParser("""
      class fail extends object[] { }
      class fail implements object[] { }
      class pass extends object implements object {}
      """)

    val classes = parser.parseFile().classes

    Weeder(classes(0)) should be === false
    Weeder(classes(1)) should be === false
    Weeder(classes(2)) should be === true
  }
}
