public class Codegen {
    public int a = 5;

    public Codegen() {
    }

    public int hashCode() {
        return 137;
    }

    public static int test() {
        Codegen c = new Codegen();

        return c.hashCode();
    }
}
