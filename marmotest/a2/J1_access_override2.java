// HIERARCHY
public class J1_access_override2 {

    public J1_access_override2 () {}

    public Object clone() { return new J1_access_override2(); }

    public static int test() {
	Object o = new J1_access_override2().clone();
        return 123;
    }

}
