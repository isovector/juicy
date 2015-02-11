// PARSER_WEEDER
public class J1_commentsInExp1 {

    public J1_commentsInExp1 () {}

    public static int test() {
	int a = - /* comments are funny */ 123;
        return -a; 
    }

}
