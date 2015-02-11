// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
/**
 * Parser/weeder:
 * - Assignment operations not allowed
 */
public class Je_1_NonJoosConstructs_AssignmentOperations_ZeroShiftRight {

    public Je_1_NonJoosConstructs_AssignmentOperations_ZeroShiftRight() {}

    public static int test() {
	int x = -492;
	x>>>=2;
	return x-1073741578;
    }
}
