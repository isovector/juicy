// JOOS1:PARSER_WEEDER,LEXER_EXCEPTION
// JOOS2:PARSER_WEEDER,LEXER_EXCEPTION
// JAVAC:UNKNOWN
// 
/**
 * Parser/weeder:
 * - '\a' is not a legal character escape sequence
 */
public class Je_1_Escapes_NonExistingEscape {

    public Je_1_Escapes_NonExistingEscape() {}

    public static int test() {
	char c = '\a'; 
	return 123;
    }
}
