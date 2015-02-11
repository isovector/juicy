// PARSER_WEEDER
// JOOS1: PARSER_EXCEPTION
// JOOS2: PARSER_EXCEPTION
// JAVAC:
/**
 * Parser/weeder:
 * - Floating point literals not allowed.
 */
public class Je_1_Literals_Float {

    public Je_1_Literals_Float() {}

    public static int test() {
	Float x = new Float(1.618034f);
	return 123;
    }
}

