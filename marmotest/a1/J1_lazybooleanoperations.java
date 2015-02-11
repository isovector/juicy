// PARSER_WEEDER
public class J1_lazybooleanoperations {
    public J1_lazybooleanoperations() {}
    public static int test() {
	boolean x = true;
	boolean y = (x && true) || x;
	return 123;
    }
}
