// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION,SYNTAX_ERROR
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION,SYNTAX_ERROR
// JAVAC:UNKNOWN
// 
/**
 * Parser/weeder:
 * - Declarations not allowed in for update
 */
public class Je_1_For_DeclarationInUpdate {

    public Je_1_For_DeclarationInUpdate() {}

    public static int test() {
	for (int i=0; i<10; int j=i) {}
	return 123;
    }

}
