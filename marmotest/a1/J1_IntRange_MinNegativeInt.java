// PARSER_WEEDER
public class J1_IntRange_MinNegativeInt {

    public J1_IntRange_MinNegativeInt () {}
    // http://www.jroller.com/comments/slava/Weblog/pitfalls_of_2_s_complement

    public static int test() {
	if ((-(-2147483648)) == (-2147483648))
	    return 123;
	return 7;
    }

    public static void main(String[] args) {
	System.out.println(J1_IntRange_MinNegativeInt.test());
    }

}
