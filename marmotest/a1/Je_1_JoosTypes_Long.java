// PARSER_WEEDER
// JOOS1: PARSER_EXCEPTION
// JOOS2: PARSER_EXCEPTION
// JAVAC:
/**
 * Parser/weeder:
 * - Type long not allowed in Joos.
 */
public class Je_1_JoosTypes_Long {

    public Je_1_JoosTypes_Long() {}

    public static int test() {
	long y = (long) 42;
	return 123;
    }
}

