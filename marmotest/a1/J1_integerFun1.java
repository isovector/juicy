// PARSER_WEEDER
public class J1_integerFun1 {

    public J1_integerFun1 () {}

    public static int test() {
	if ((-2147483648 + -2147483648) == 0)
	    return 123;
        return 7;
    }

    public static void main(String[] args) {
	System.out.println(J1_integerFun1.test());
    }
}
