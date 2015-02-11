// PARSER_WEEDER
// JOOS1: PARSER_EXCEPTION
// JOOS2: PARSER_EXCEPTION
// JAVAC:
/**
 * Parser/weeder:
 * - Type double not allowed in Joos
 */
public class Je_1_JoosTypes_Double {

    public Je_1_JoosTypes_Double() {}

    public static int test() {
	double y = Math.PI;
	return 123;
    }
}

