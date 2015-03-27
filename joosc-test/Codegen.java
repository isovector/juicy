public class Codegen {
    public Codegen inner;
    public int value;

    public Codegen() {
        this.value = 5;
    }

    public Codegen(boolean print) {
        this.inner = new Codegen();
    }

    public static int test() {
        Codegen c = new Codegen(true);
        return c.inner.value;
    }
}
