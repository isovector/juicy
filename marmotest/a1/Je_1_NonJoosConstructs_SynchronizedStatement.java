// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:
/**
 * Parser/weeder:
 * - Synchronized statements not allowed in joos
 */
public class Je_1_NonJoosConstructs_SynchronizedStatement{

    public Je_1_NonJoosConstructs_SynchronizedStatement(){}

    public static int test() {
	Integer x = new Integer(16);
	synchronized (x) {
	    x = new Integer(42);
	}
	return 123;
    }
    
}
