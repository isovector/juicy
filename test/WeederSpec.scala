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
      abstract final public class Fail { public Fail() {} }
      final public class Pass { public Pass(){} }
      abstract public class Pass { public Pass(){} }
      """)

    val classes = parser.parseFile().classes

    Weeder(classes(0)) should be === false
    Weeder(classes(1)) should be === true
    Weeder(classes(2)) should be === true
  }

  it should "fail abstract method bodies" in {
    val parser = mkParser("""
      public class Class {
        public Class (int i) {}
        abstract public bool fail() { }
      }

      public class Class {
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
      public class Class {
        public Class (String s) {}
        abstract public static bool fail();
      }

      public class Class {
        public Class (String s) {}
        abstract public final bool fail();
      }

      public class Class {
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
      public class Class {
        public Class (Class other) {}
        static public final bool fail();
      }

      public class Class {
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
      public class Class {
        public Class() {}
        native public bool fail();
      }

      public class Class {
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
      public class Class {
        public Class() {}
        public bool fail(void a);
      }

      public class Class {
        public Class() {}
        public bool fail() {
          void a = 5;
        }
      }

      public class Class {
        public Class() {}
        public bool fail() {
          a = (void)5;
        }
      }

      public class Class {
        public Class() {}
        public bool fail() {
          a = new void[5];
        }
      }

      public class Class {
        public Class() {}
        public void[] fail() {
        }
      }

      public class Class {
        public Class() {}
        public void pass() { }
      }
      """)

    val classes = parser.parseFile().classes

    Weeder(classes(0)) should be === false
    Weeder(classes(1)) should be === false
    Weeder(classes(2)) should be === false
    Weeder(classes(3)) should be === false
    Weeder(classes(4)) should be === false
    Weeder(classes(5)) should be === true
  }

  it should "not allow final fields" in {
    val parser = mkParser("""
      public class Class {
        public Class() {}
        public final bool fail;
      }

      public class Class {
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
      public class fail extends object[] { public fail() {} }
      public class fail implements object[] { public fail() {}}
      public class pass extends object implements object {public pass() {} }
      """)

    val classes = parser.parseFile().classes

    Weeder(classes(0)) should be === false
    Weeder(classes(1)) should be === false
    Weeder(classes(2)) should be === true
  }

  it should "not allow classes without at least one explicit constructor" in {
    val parser = mkParser("""
      public class Fail {}
      public class Fail {
        public void doSomething() {}
      }
      public class Pass {
        public Pass() {}
      }
      final public class Pass {
        public Pass(int i) {
          i = i + 1;
        }
      }
      abstract public class Pass {
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
      public interface Fail {
        public Fail();
      }
      protected interface Succeed {
        public boolean succeed();
      }
      public interface Fail {
        public boolean fail() {
          System.out.println("whoops");
        }
      }
      public interface Fail {
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

  it should "not allow instanceof primitive types" in {
    val parser = mkParser("""
      test instanceof boolean;
      5 instanceof int[];
      5 instanceof Boolean;
      """)

    Weeder(parser.parseStmnt()) should be === false
    Weeder(parser.parseStmnt()) should be === true
    Weeder(parser.parseStmnt()) should be === true
  }

  it should "fail classes with multiple extends" in {
    val parser = mkParser("""
      class A extends AA, AB { }
      """)

    Weeder(parser.parseFile()) should be === false
  }

  it should "allow interfaces with multiple extends" in {
    val parser = mkParser("""
      interface A extends AA, AB { }
      """)

    Weeder(parser.parseFile()) should be === false
  }

  it should "not allow arbitrary calls" in {
    val parser = mkParser("""
      5();
      true();
      (1 + 5)();
      a[5]();
      a();
      a.b();
      """)

    Weeder(parser.parseStmnt()) should be === false
    Weeder(parser.parseStmnt()) should be === false
    Weeder(parser.parseStmnt()) should be === false
    Weeder(parser.parseStmnt()) should be === false
    Weeder(parser.parseStmnt()) should be === true
    Weeder(parser.parseStmnt()) should be === true
  }

  it should "not allow super or this calls" in {
    val parser = mkParser("""
      this.hello();
      this();
      super();
      super(5);
      super.up.the.chain(true);
      test(this.sup);
      works.great();
      """)

    Weeder(parser.parseStmnt()) should be === true
    Weeder(parser.parseStmnt()) should be === false
    Weeder(parser.parseStmnt()) should be === false
    Weeder(parser.parseStmnt()) should be === false
    Weeder(parser.parseStmnt()) should be === false
    Weeder(parser.parseStmnt()) should be === true
    Weeder(parser.parseStmnt()) should be === true
  }
}
