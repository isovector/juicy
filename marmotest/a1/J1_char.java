// PARSER_WEEDER
public class J1_char {
    public J1_char() {}
    protected char x = (char)123;
    public static int test() {
	J1_char obj = new J1_char();
	char y = obj.x;
	return (int)y;
    }
}

