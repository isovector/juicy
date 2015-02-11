// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:
/**
 * Parser/weeder:
 * - Break statement not allowed.
 */
public class Je_1_NonJoosConstructs_Break {

    public Je_1_NonJoosConstructs_Break() {}

    public static int test() {
	int x = 117;
	while (x>0) {
	    x=x-1;
	    if (x==42) { 
		break; 
	    }
	}
	return 123;
    }
}

