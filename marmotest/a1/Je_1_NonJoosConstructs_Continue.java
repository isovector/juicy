// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
/**
 * Parser/weeder:
 * - Continue statements not allowed.
 */
public class Je_1_NonJoosConstructs_Continue {

    public Je_1_NonJoosConstructs_Continue() {}

    public static int test() {
	int x = 117;
	while (x>0) {
		x = x - 1;
	    continue;
	}
	return 123;
    }
}

