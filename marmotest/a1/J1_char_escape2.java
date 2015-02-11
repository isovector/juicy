// PARSER_WEEDER
public class J1_char_escape2 {
    public J1_char_escape2() {}
    public static int test() {
	int r = 0;
	if (J1_char_escape2.eq("\\b",   ((char)92+"b"))) r=r+1;
	if (J1_char_escape2.eq("\\f",   (char)92+"f")) r=r+1;
	if (J1_char_escape2.eq("\\n",   (char)92+"n")) r=r+1;
	if (J1_char_escape2.eq("\\r",   (char)92+"r")) r=r+1;
	if (J1_char_escape2.eq("\\'",   (char)92+"'")) r=r+1;
	if (J1_char_escape2.eq("'\\\'", "'"+(char)92+"'")) r=r+1;
	if (J1_char_escape2.eq("'\\\"", "'"+(char)92+(char)34)) r=r+1;
	return r+116;
    }
    public static boolean eq(String a, String b) {return a.equals((Object)b);}
}
