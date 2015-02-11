// PARSER_WEEDER
public class J1_octal_escape5 {
    public J1_octal_escape5() {}
    public static int test() {
	String s = "\421abc\2400xyz\377\19\400";
	String s2 = (char)34 + "1abc" + (char)160 + "0xyz" + (char)255 + (char)1 + "9 0";
	if (s.equals((Object)s2)) return 123;
	else return 0;
    }
}
