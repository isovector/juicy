// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:
/**
 * Parser/weeder:
 * - Keyword volatile not allowed in Joos
 */
public class Je_1_NonJoosConstructs_Volatile {

    public Je_1_NonJoosConstructs_Volatile() {}

    public volatile int x;

    public static int test() {
	return 123;
    }
}
