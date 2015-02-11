// PARSER_WEEDER
public class J1_if {
    public J1_if() {}
    public static int test() {
	return new J1_if().m(-117);
    }
    public int m(int x) {
	int y = 0;
	if (x==0) y=42;
	else y=123;
	return y;
    }
}

