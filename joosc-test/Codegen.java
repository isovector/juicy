public class Codegen {
    public Codegen() { }

    public static int test() {
        return Codegen.print(77, 99);
    }

    public static int print(int a, int b) {
        int c = 15;
        int d = 9;
        #YOLO "sup sexy";
        return c;
    }
}
