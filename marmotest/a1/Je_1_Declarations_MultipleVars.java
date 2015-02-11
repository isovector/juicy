// PARSER_WEEDER
// JOOS1:PARSER_EXCEPTION
// JOOS2:PARSER_EXCEPTION
// JAVAC:
/**
 * Parser/weeder:
 * - Multiple variable in same declaration not allowed. 
 */
public class Je_1_Declarations_MultipleVars {

    public Je_1_Declarations_MultipleVars() {}

    public static int test() {
	int i=0, j=10;
	return 123;
    }

}
