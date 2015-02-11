// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:
/**
 * Parser/weeder:
 * - Private methods not allowed in Joos
 */
public class Je_1_NonJoosConstructs_PrivateMethods {

    public Je_1_NonJoosConstructs_PrivateMethods () {}

    private int m() {
	return 42;
    }

    public static int test() {
	return 123;
    }
}
