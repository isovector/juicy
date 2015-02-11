// PARSER_WEEDER
// JOOS1: PARSER_EXCEPTION
// JOOS2: PARSER_EXCEPTION
// JAVAC:
/**
 * Parser/weeder:
 * - Class literals not allowed.
 */
public class Je_1_Literals_Class {

    public Je_1_Literals_Class() {}

    public static int test() {
	java.lang.Class y = Object.class;
	return 123;
    }
}
