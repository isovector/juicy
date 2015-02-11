// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
/**
 * Parser/weeder:
 * - do while construct not allowed
 */
public class Je_1_NonJoosConstructs_DoWhile {

    public Je_1_NonJoosConstructs_DoWhile() {}

    public static int test() {
	int x = 13;
	do { 
	    x = x-1; 
	} while (x > 0);
	return 123;
    }
}

