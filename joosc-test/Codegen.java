public class Codegen {
    public int magic = 88;

    public Codegen() {
        #YOLO "empty ctor";
    }

    public Codegen(boolean print) {
        #YOLO "bool ctor";
        this.magic = 10;
    }

    public static int test() {
        Codegen c = new Codegen(true);
        return c.magic;
    }
}
