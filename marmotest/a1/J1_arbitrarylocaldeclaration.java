// PARSER_WEEDER,ENVIRONMENTS
public class J1_arbitrarylocaldeclaration {
    public static int test() {
	return new J1_arbitrarylocaldeclaration().m();
    }
    public J1_arbitrarylocaldeclaration() {}
    public int m() {
	int x = 35;
	x = x+1;
	int y = x+87;
	return y;
    }
}
