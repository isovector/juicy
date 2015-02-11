// PARSER_WEEDER
// JOOS1: PARSER_EXCEPTION
// JOOS2: PARSER_EXCEPTION
// JAVAC:
/**
 * Parser/weeder:
 * - Long literals not allowed in Joos.
 */
public class Je_1_Literals_Long {

    public Je_1_Literals_Long() {}

    public static int test() {
	String s = Long.toOctalString(123L);
	return 123;
    }
}

