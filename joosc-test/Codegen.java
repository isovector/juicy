public class Codegen {
    public Codegen() {
    }

    public static boolean lazy(boolean b) {
#YOLO "lazy called"
        return b;
    }

    public static int test() {
        boolean b = false;

        if (b || Codegen.lazy(true)) {
            #YOLO "good";
        }

        return 0;
    }
}
