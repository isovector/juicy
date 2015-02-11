// PARSER_WEEDER
public class J1_char_escape3 {
    public J1_char_escape3() {}
    public static int test() {
	int r = 0;
	if (J1_char_escape3.eq("\\123", ((char)92+"123"))) r=r+1;
	if (J1_char_escape3.eq("\\12", ((char)92+"12"))) r=r+1;
	if (J1_char_escape3.eq("\\1", ((char)92+"1"))) r=r+1;
	if (J1_char_escape3.eq("\134123", ((char)92+"123"))) r=r+1;
	if (J1_char_escape3.eq("\13412", ((char)92+"12"))) r=r+1;
	if (J1_char_escape3.eq("\1341", ((char)92+"1"))) r=r+1;
	return r+117;
    }
    public static boolean eq(String a, String b) {return a.equals((Object)b);}
}
