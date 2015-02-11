// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:
// 
/**
 * Parser/weeder:
 * - Assignment operations not allowed
 */
public class Je_1_NonJoosConstructs_AssignmentOperations_BitwiseXOR {

    public Je_1_NonJoosConstructs_AssignmentOperations_BitwiseXOR() {}

    public static int test() {
	int x = 42;
	x^=81;
	return x;
    }
}
