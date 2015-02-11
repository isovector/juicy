// JOOS1:PARSER_WEEDER,LEXER_EXCEPTION
// JOOS2:PARSER_WEEDER,LEXER_EXCEPTION
/**
 * Parser/weeder:
 * - Choice operator not permitted
 */
public class Je_1_NonJoosConstructs_Choice {

    public Je_1_NonJoosConstructs_Choice() {}

    public static int test() {
	int x = 123;
	return x==42 ? 87 : x; 
    }
}
