// PARSER_WEEDER
public class J1_publicmethods {
    public J1_publicmethods() {}
    public int m() {
	return 123;
    }
    public static int test() {
	return new J1_publicmethods().m();
    }
}
