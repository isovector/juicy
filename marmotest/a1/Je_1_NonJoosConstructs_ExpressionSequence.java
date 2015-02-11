// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:UNKNOWN
/**
 * Parser/weeder:
 * - Expression sequencing not allowed.
 */
public class Je_1_NonJoosConstructs_ExpressionSequence {

    public Je_1_NonJoosConstructs_ExpressionSequence() {}

    public static int test() {
	int i = 0;
	int j= (i = i + 1, i); 
	return 123;
    }

}
