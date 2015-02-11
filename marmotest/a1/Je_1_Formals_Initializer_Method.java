// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:UNKNOWN
// 
/**
 * Parser/weeder:
 * - A formal parameter must not have an initializer.
 */
public class Je_1_Formals_Initializer_Method {

    public Je_1_Formals_Initializer_Method() {}

    public static int test(int i = 123) {
	return 123;
    }
}
