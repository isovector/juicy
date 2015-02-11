// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// 
/**
 * Parser/weeder:
 * - Assignment operations not allowed
 */
public class Je_1_NonJoosConstructs_AssignmentOperations_ShiftLeft {

    public Je_1_NonJoosConstructs_AssignmentOperations_ShiftLeft() {}

    public static int test() {
	int x = 30;
	x<<=2;
	return x+3;
    }
}
