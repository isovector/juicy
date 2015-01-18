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
        abstract public bool fail() { }
      }

      class Class {
        public Class (int i) {}
        abstract public bool pass();
      }
      """)

    val classes = parser.parseFile().classes

    Weeder(classes(0)) should be === false
    Weeder(classes(1)) should be === true
  }

  it should "fail static final abstract methods" in {
    val parser = mkParser("""
      class Class {
        public Class (String s) {}
        abstract public static bool fail();
      }

      class Class {
        public Class (String s) {}
        abstract public final bool fail();
      }

      class Class {
        public Class(String s) {}
        abstract public bool pass();
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
        static public final bool fail();
      }

      class Class {
        public Class() {}
        static public bool pass() {}
        final public bool pass() {}
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
        native public bool fail();
      }

      class Class {
        public Class() {}
        static native public bool pass();
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
        public bool fail(void a);
      }

      class Class {
        public Class() {}
        public bool fail() {
          void a;
        }
      }

      class Class {
        public Class() {}
        public void pass() { }
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
        public final bool fail;
      }

      class Class {
        public Class() {}
        public bool pass;
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
        public void doSomething() {}
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
           public boolean succeed();
       }
       interface Fail {
           public boolean fail() {
              System.out.println("whoops");
           }
       }
       interface Fail {
           public boolean succeed();
           public int utterFailure;
       }
     """)
    val classes =  parser.parseFile().classes
    Weeder(classes(0)) should be === false
    Weeder(classes(1)) should be === true
    Weeder(classes(2)) should be === false
    Weeder(classes(3)) should be === false
  }

  it should "not allow lvalue casts" in {
    val parser = mkParser("""
      (int)a = 5;
      a = 5;
      """)

    Weeder(parser.parseStmnt()) should be === false
    Weeder(parser.parseStmnt()) should be === true
  }

  it should "not allow instantiation of primitive types" in {
    val parser = mkParser("""
      new int();
      new B();
      """)

    Weeder(parser.parseStmnt()) should be === false
    Weeder(parser.parseStmnt()) should be === true
  }
}
