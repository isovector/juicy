// JOOS1: PARSER_WEEDER,JOOS1_THIS_CALL,PARSER_EXCEPTION
// JOOS2: TYPE_CHECKING,NO_MATCHING_CONSTRUCTOR_FOUND
// JAVAC:UNKNOWN
/**
 * Parser/weeder:
 * - (Joos 1) No explicit super or this statements allowed.
 * Typecheck:
 * - (Joos 2) Check that any method or constructor invocation
 * resolves to a uniquely closest matching method or constructor
 * (15.12.2).  
 */
public class Je_16_SuperThis_InvalidThisParameter {

    public Je_16_SuperThis_InvalidThisParameter() {
	this(42);
    }

    public Je_16_SuperThis_InvalidThisParameter(String s) {}
    
    public static int test() { 
	return 123; 
    }
}


