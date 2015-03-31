public class Codegen {
    public int a = 40;

    public Codegen() {
    }

    public void mutate() {
        a = a / 2;
    }

    public static int test() {
        Codegen c = new Codegen();
        c.mutate();
        c.mutate();
        return c.a;
    }
}
