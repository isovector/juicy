// PARSER_WEEDER
public class J1_octal_escape4 {
    public J1_octal_escape4() {}
    public static int test() {
	String s = "\3\20\100(";
	int r = 0;
	for (int i=0; i<s.length(); i=i+1) r=r+(int)s.charAt(i);
	return r;
    }
}
