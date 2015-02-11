// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:
/**
 * Parser/weeder:
 * - Unary plus operator not allowed in Joos
 */
public class Je_1_NonJoosConstructs_UnaryPlus {

    public Je_1_NonJoosConstructs_UnaryPlus() {}

    public static int test() {
	int x = 123;
	return +x; 
    }
}
