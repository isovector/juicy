// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
/**
 * Parser/weeder:
 * - Bitshift operators not allowed
 */
public class Je_1_NonJoosConstructs_BitShift_ZeroRight {

    public Je_1_NonJoosConstructs_BitShift_ZeroRight() {}
    
    public static int test() {
	int x = -492;
	return (x >>> 2) - 1073741578;
    }
}

