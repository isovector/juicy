// PARSER_WEEDER
// JOOS1: PARSER_EXCEPTION
// JOOS2: PARSER_EXCEPTION
// JAVAC:
/**
 * Parser/weeder:
 * - Type float not allowed.
 */
public class Je_1_JoosTypes_Float {

    public Je_1_JoosTypes_Float() {}

    public static int test() {
	float y = Float.MAX_VALUE;
	return 123;
    }
}

