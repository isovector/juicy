// PARSER_WEEDER
public class J1_publicfields {
    protected J1_publicfields() {
	this.x = 123;
    }
    public int x;
    public static int test() {
	return new J1_publicfields().x;
    }
}

