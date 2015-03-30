public class Codegen {
    public int a = 5;

    public Codegen() {
    }

    public int hashCode() {
        return 137;
    }

    public int foo() {
      return hashCode();
    }

    public static int test() {
        Codegen c = new Codegen2();
        return c.foo();
    }
}
