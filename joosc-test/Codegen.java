public class Codegen implements FunInterface {
    public Codegen inner;
    public int val;

    public Codegen() {
    }

    public Codegen(int x) {
        inner = new Codegen();
        inner.val = x;
    }
    
    public int foo(char c) {
      return 0;
    }

    public static int test() {
        return "hello".chars[0];
    }
}
