// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
/**
 * Parser/weeder:
 * - Bitshift operators not allowed
 */
public class Je_1_NonJoosConstructs_BitShift_Left {

    public Je_1_NonJoosConstructs_BitShift_Left() {}
    
    public static int test() {
	int x = 30;
	return (x << 2) + 3;
    }
}

