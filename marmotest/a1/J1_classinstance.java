// PARSER_WEEDER
public class J1_classinstance {

    public int foo;
    
    public J1_classinstance() {}
    
    public static int test() {
        (new J1_classinstance()).foo = 42;
	new J1_classinstance().foo = 42;
	return 123;
    }
 
}
