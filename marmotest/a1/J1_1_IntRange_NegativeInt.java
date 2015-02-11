// PARSER_WEEDER
public class J1_1_IntRange_NegativeInt {
    public J1_1_IntRange_NegativeInt() {}
    
    public static int test() {
	int a = -2147483648;
	if (a-1 > a) 
	    return 123;
	return 42;
    }
}
