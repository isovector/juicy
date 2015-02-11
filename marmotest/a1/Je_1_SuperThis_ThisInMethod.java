// PARSER_WEEDER,
// JOOS1: JOOS1_THIS_CALL,THIS_CALL_NOT_FIRST_STATEMENT,PARSER_EXCEPTION
// JOOS2: THIS_CALL_NOT_FIRST_STATEMENT,PARSER_EXCEPTION
// JAVAC: UNKNOWN
/**
 * Parser/weeder:
 * - (Joos 1) No explicit super or this statements allowed
 * - (Joos 2) A super or this statement must be the first statement in
 *   a constructor body.
 */
public class Je_1_SuperThis_ThisInMethod {
	
    public Je_1_SuperThis_ThisInMethod(){}
    
    public int test() {
	this();
	return 123;
    }
    
}
