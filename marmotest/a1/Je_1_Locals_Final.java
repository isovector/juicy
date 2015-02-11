// PARSER_WEEDER
// JOOS1: PARSER_EXCEPTION
// JOOS2: PARSER_EXCEPTION
// JAVAC:
/**
 * Parser/weeder:
 * - Local variables must not be final
 */
public class Je_1_Locals_Final {

    public Je_1_Locals_Final() {}

    public static int test() {
	final int x = 123;
	return 123;
    }
}
 
