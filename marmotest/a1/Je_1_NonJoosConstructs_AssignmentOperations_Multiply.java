// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:
// 
/**
 * Parser/weeder:
 * - Assignment operations not allowed
 */
public class Je_1_NonJoosConstructs_AssignmentOperations_Multiply {

    public Je_1_NonJoosConstructs_AssignmentOperations_Multiply() {}

    public static int test() {
	int x = 3;
	x*=41;
	return x;
    }
}
