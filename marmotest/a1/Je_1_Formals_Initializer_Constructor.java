// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:UNKNOWN
// 
/**
 * Parser/weeder:
 * - A formal parameter must not have an initializer.
 */
public class Je_1_Formals_Initializer_Constructor {

    public Je_1_Formals_Initializer_Constructor(int abc=123) {}

    public static int test() {
	return 123;
    }
}
