// ENVIRONMENTS,DISAMBIGUATION
public class J1_formalindex {

    protected int field;

    public J1_formalindex() {
    }

    public J1_formalindex(int a, int b, int c, int d, int e) {
	this.field = foo(a,b,c,d,e);
    }

    protected int foo(int a, int b, int c, int d, int e) {
	int local = a * c;
	return local+e;
    }

    public static int test() {
        return new J1_formalindex(11,42,2,88,101).field;
    }

}
