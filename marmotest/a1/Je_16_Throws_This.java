// JOOS1: PARSER_WEEDER,JOOS1_THIS_CALL,PARSER_EXCEPTION
// JOOS2: TYPE_CHECKING,ILLEGAL_THROWS
// JAVAC:UNKNOWN
/**
 * Parser/weeder:
 * - (Joos 1) No explicit super or this statements allowed.
 * Typecheck:
 * - For every method invocation expression, class instance creation
 * expression, implicit or explicit super constructor invocation
 * statement or explicit this constructor invocation statement in the
 * program, check that for every checked exception E2 declared in the
 * throws clause of the invoked method or constructor, the current
 * method or constructor declares an exception E1 in its throws clause
 * such that E2 is a subclass of E1. 
 */
public class Je_16_Throws_This {
    
    public Je_16_Throws_This(int i) throws CloneNotSupportedException {}
    
    public Je_16_Throws_This() {
	this(100);
    }
    
    public static int test() {
	return 123;
    }
}
