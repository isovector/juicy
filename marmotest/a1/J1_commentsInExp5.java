// PARSER_WEEDER
public class J1_commentsInExp5 {

    public J1_commentsInExp5 () {}

    public static int test() {
	String s = (String) /* String conversion */ "String";
        return 123;
    }

}
