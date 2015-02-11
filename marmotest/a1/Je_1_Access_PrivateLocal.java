// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:UNKNOWN
/**
 * Parser/weeder:
 * - A local variable must not have an access modifier.
 */
public class Je_1_Access_PrivateLocal{

    public Je_1_Access_PrivateLocal(){}

    public static int test(){
	private int a = 123;
	return a;
    }
}
