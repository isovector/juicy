// PARSER_WEEDER
// JOOS1: PARSER_EXCEPTION
// JOOS2: PARSER_EXCEPTION
// JAVAC:
/**
 * Parser/weeder:
 * - Floating point literals not allowed.
 */
public class Je_1_Literals_Exponential {

    public Je_1_Literals_Exponential() {}

    public static int test() {
	Double x = new Double(6.0221415e23);
	return 123;
    }
}

