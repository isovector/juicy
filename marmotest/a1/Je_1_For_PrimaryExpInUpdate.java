// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION,SYNTAX_ERROR
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION,SYNTAX_ERROR
// JAVAC:UNKNOWN
// 
/**
 * Parser/weeder:
 * - Primary expression not allowed in update clause of for loop
 */
public class Je_1_For_PrimaryExpInUpdate {

    public Je_1_For_PrimaryExpInUpdate() {}

    public static int test() {
	int x = 0;
	for (x=1; x<10; 42) { }
	return 123;
    }

}
