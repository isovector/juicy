// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
/**
 * Parser/weeder:
 * - Bitwise operators not allowed
 */
public class Je_1_NonJoosConstructs_Bitwise_Negation {

    public Je_1_NonJoosConstructs_Bitwise_Negation() {}
    
    public static int test() {
	int x = 30;
	return ~x;
    }
}

