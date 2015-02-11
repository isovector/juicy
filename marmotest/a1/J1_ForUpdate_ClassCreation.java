// PARSER_WEEDER
public class J1_ForUpdate_ClassCreation {
    public J1_ForUpdate_ClassCreation() {}
    
    public static int test() {
	for (int i = 0 ; i < 10 ; new String("foo")) {
	    i = 10;
	}
	return 123;
    }
}
