public class Codegen {
    public Codegen() { }

    public static int test() {
        Codegen.print();
        #YOLO "return worked";
        return 99;
    }

    public static void print() {
        #YOLO "sup sexy";
    }
}
