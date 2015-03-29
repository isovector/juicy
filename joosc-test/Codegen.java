public class Codegen {
    public Codegen() {
    }

    public static int test() {
        int magic = 18;

        if (magic < 19) {
            int a = 5;
            #YOLO "good";
        }

        return 0;
    }
}
