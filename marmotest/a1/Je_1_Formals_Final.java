// PARSER_WEEDER
// JOOS1:PARSER_EXCEPTION,SYNTAX_ERROR
// JOOS2:PARSER_EXCEPTION,SYNTAX_ERROR
// JAVAC:
/**
 * Parser/weeder:
 * - Formal parameters must not be final
 */
public class Je_1_Formals_Final {

    public Je_1_Formals_Final() {}

    public int m(final int x) {
	return 123;
    }

    public static int test() {
	return 123;
    }
}

 
