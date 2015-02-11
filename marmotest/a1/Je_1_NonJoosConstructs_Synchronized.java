// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:
/**
 * Parser/weeder:
 * - Synchronized modifier not allowed in joos
 */
public class Je_1_NonJoosConstructs_Synchronized{

    public Je_1_NonJoosConstructs_Synchronized(){}

    public synchronized void m(){
    }

    public static int test(){
	return 123;
    }
}
