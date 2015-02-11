// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
/**
 * Parser/weeder:
 * - Assignment operations not allowed
 */
public class Je_1_NonJoosConstructs_AssignmentOperations_Remainder {

    public Je_1_NonJoosConstructs_AssignmentOperations_Remainder() {}

    public static int test() {
	int x = 15375;
	x%=124;
	return x;
    }
}
