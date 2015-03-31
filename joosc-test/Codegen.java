public class Codegen implements Cloneable {
    public Codegen inner;
    public int val;

    public Codegen() {
    }

    public Codegen(int x) {
        inner = new Codegen();
        inner.val = x;
    }

    public static int test() {
        Codegen c = new Codegen(5);
        return c.inner.val;
    }
}
