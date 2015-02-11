// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
/**
 * Parser/weeder:
 * - Assignment operations not allowed
 */
public class Je_1_NonJoosConstructs_AssignmentOperations_SignShiftRight {

    public Je_1_NonJoosConstructs_AssignmentOperations_SignShiftRight() {}

    public static int test() {
	int x = -492;
	x>>=2;
	return x+246;
    }
}
