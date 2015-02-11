// ENVIRONMENTS
public class J1_local_duplicate {
    public J1_local_duplicate() {}
    protected int r = 456;
    public static int test() {
	{{
	    int r = 123;
	    return r;
	}}
    }
}
