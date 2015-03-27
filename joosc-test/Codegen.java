public class Codegen {
    public int magic = 88;

    public Codegen() { }

    public static int test() {
        int newValue = 5;
        newValue = 6;

        Codegen c = new Codegen();
        c.magic = newValue;
        Codegen.mutate(c);

        return c.magic;
    }

    public static void mutate(Codegen c) {
        c.magic = 20;
    }
}
