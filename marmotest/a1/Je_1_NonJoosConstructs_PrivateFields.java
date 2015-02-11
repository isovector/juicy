// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:
/**
 * Parser/weeder:
 * - Private fields not allowed in Joos.
 */
public class Je_1_NonJoosConstructs_PrivateFields {

    private int x;

    public Je_1_NonJoosConstructs_PrivateFields() {}

    public static int test() {
	return 123;
    }
}
