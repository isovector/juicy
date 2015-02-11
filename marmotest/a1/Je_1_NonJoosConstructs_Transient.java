// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:
/**
 * Parser/weeder:
 * - Transient keyword not allowed in Joos
 */
public class Je_1_NonJoosConstructs_Transient {

    public Je_1_NonJoosConstructs_Transient() {}

    public transient int x;

    public static int test() {
	return 123;
    }
}
