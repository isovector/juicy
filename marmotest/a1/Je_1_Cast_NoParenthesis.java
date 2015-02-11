// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:UNKNOWN
/**
 * Parser/weeder:
 * - Type clause in cast must be enclosed in parentheses
 */
public class Je_1_Cast_NoParenthesis {

    public Je_1_Cast_NoParenthesis(){}

    public static int test() {
	int foo = -123;
	return int-foo;
    }
}
