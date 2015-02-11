// PARSER_WEEDER
public class J1_integerFun {

    public J1_integerFun () {}

    public static int test() {
	if ((65536*65536) == (16777216*256))
	    return 123;
        return 7;
    }

    public static void main(String[] args) {
	System.out.println(J1_integerFun.test());
    }

}
