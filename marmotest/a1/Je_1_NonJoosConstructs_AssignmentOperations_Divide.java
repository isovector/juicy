// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:
// 
/**
 * Parser/weeder:
 * - Assignment operations not allowed
 */
public class Je_1_NonJoosConstructs_AssignmentOperations_Divide {

    public Je_1_NonJoosConstructs_AssignmentOperations_Divide() {}

    public static int test() {
	int x = 246;
	x/=2;
	return x;
    }
}
