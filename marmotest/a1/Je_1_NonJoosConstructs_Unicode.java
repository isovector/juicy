// JOOS1:PARSER_WEEDER,LEXER_EXCEPTION
// JOOS2:PARSER_WEEDER,LEXER_EXCEPTION
// JAVAC:
/**
 * Parser/weeder:
 * - Unicode input not allowed in Joos
 */
public class Je_1_NonJoosConstructs_Unicode {

    public Je_1_NonJoosConstructs_Unicode() {}

    public static int test(){
	String x = "\u16aa";
	return 123;
    }
}
