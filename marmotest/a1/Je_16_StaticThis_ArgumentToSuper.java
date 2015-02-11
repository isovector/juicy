// JOOS1: PARSER_WEEDER,JOOS1_EXPLICIT_SUPER_CALL,PARSER_EXCEPTION
// JOOS2: TYPE_CHECKING,THIS_BEFORE_SUPER_CALL
// JAVAC:UNKNOWN
/**
 * Parser/weeder:
 * - (Joos 1) Explicit this statements not allowed
 * Typecheck:
 * - A this reference (AThisExp) must not occur, explicitly or
 * implicitly, in a static method, an initializer for a static field,
 * or an argument to a super or this constructor invocation.  
 */
public class Je_16_StaticThis_ArgumentToSuper extends Thread{

    public Je_16_StaticThis_ArgumentToSuper(){
	super(this);
    }

    public static int test(){
	return 123;
    }
}
