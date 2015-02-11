// PARSER_WEEDER
// JOOS1: PARSER_EXCEPTION,SYNTAX_ERROR
// JOOS2: PARSER_EXCEPTION,SYNTAX_ERROR
// JAVAC: 
/**
 * Parser/weeder:
 * - Multiple expressions not allowed in for update
 */
public class Je_1_For_MultipleUpdates{

    public Je_1_For_MultipleUpdates(){}

    public static int test(){
	for (int i = 0; i < 10; i = i + 1, i = i + 2){}
	return 123;
    }
}
