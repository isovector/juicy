import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

import juicy.source.parser._
import juicy.source.tokenizer._
import juicy.source.weeder._

class WeederSpec extends FlatSpec with ShouldMatchers {
  import juicy.source.tokenizer.Token._

  // We don't have files. Don't check files
  Weeder.debug.checkFileName = false

  def mkParser(source: String) = new Parser(new TokenStream(source))

  "Weeder" should "fail abstract and final classes" in {
    val parser = mkParser("""
      abstract final class Fail { public Fail() {} }
      final class Pass { public Pass(){} }
      abstract class Pass { public Pass(){} }
      """)

    val classes = parser.parseFile().classes

    Weeder(classes(0)) should be === false
    Weeder(classes(1)) should be === true
    Weeder(classes(2)) should be === true
  }

  it should "fail abstract method bodies" in {
    val parser = mkParser("""
      class Class {
        public Class (int i) {}
        abstract bool fail() { }
      }

      class Class {
        public Class (int i) {}
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
        Class (String s) {}
        abstract static bool fail();
      }

      class Class {
        Class (String s) {}
        abstract final bool fail();
      }

      class Class {
        Class(String s) {}
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
        public Class (Class other) {}
        static final bool fail();
      }

      class Class {
        public Class() {}
        static bool pass() {}
        final bool pass() {}
      }
      """)

    val classes = parser.parseFile().classes

    Weeder(classes(0)) should be === false
    Weeder(classes(1)) should be === true
  }

  it should "ensure native methods be static" in {
    val parser = mkParser("""
      class Class {
        public Class() {}
        native bool fail();
      }

      class Class {
        public Class() {}
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
        public Class() {}
        bool fail(void a);
      }

      class Class {
        public Class() {}
        bool fail() {
          void a;
        }
      }

      class Class {
        public Class() {}
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
        public Class() {}
        final bool fail;
      }

      class Class {
        public Class() {}
        bool pass;
      }
      """)

    val classes = parser.parseFile().classes

    Weeder(classes(0)) should be === false
    Weeder(classes(1)) should be === true
  }

  it should "not allow array implementations or extends" in {
    val parser = mkParser("""
      class fail extends object[] { public fail() {} }
      class fail implements object[] { public fail() {}}
      class pass extends object implements object {public pass() {} }
      """)

    val classes = parser.parseFile().classes

    Weeder(classes(0)) should be === false
    Weeder(classes(1)) should be === false
    Weeder(classes(2)) should be === true
  }
  it should "not allow classes without at least one explicit constructor" in {
    val parser = mkParser("""
    class Fail {}
    class Fail {
        void doSomething() {}
    }
    class Pass {
        public Pass() {}
    }
    final class Pass {
        public Pass(int i) {
           i = i + 1;
        }
    }
    abstract class Pass {
        public Pass() {
            int i = 7;
        }
        public Pass(int i) {
            i = 7;
        }
    }
    """)
    val classes =  parser.parseFile().classes
    Weeder(classes(0)) should be === false
    Weeder(classes(1)) should be === false
    Weeder(classes(2)) should be === true
    Weeder(classes(3)) should be === true
    Weeder(classes(4)) should be === true
  }
  it should "not allow interfaces with constructors, method bodies or fields" in {
     val parser = mkParser("""
       interface Fail {
           public Fail();
       }
       interface Succeed {
           boolean succeed();
       }
       interface Fail {
           boolean fail() {
              System.out.println("whoops");
           }
       }
       interface Fail {
           boolean succeed();
           int utterFailure;
       }
     """)
    val classes =  parser.parseFile().classes
    Weeder(classes(0)) should be === false
    Weeder(classes(1)) should be === true
    Weeder(classes(2)) should be === false
    Weeder(classes(3)) should be === false
  }
}
