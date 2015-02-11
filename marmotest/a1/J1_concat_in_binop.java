// PARSER_WEEDER,CODE_GENERATION
public class J1_concat_in_binop {
    public J1_concat_in_binop() {}
    public static int test() {
	String x = "";
	String y = ("Result: "+("abc"+x == null)); // "Result: false"
	return 110+y.length();
    }
}
