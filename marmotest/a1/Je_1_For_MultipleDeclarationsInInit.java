// PARSER_WEEDER
// JOOS1: PARSER_EXCEPTION,SYNTAX_ERROR
// JOOS2: PARSER_EXCEPTION,SYNTAX_ERROR
// JAVAC: 
/**
 * Parser/weeder:
 * - Multiple declarations not allowed in for initializer.
 */
public class Je_1_For_MultipleDeclarationsInInit{

    public Je_1_For_MultipleDeclarationsInInit(){}

    public static int test(){
	for (int i = 0, j = 10; i < j; i = i + 1){}
	return 123;
    }
}
