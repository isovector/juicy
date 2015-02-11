// TYPE_CHECKING
// JOOS1: PARSER_WEEDER,JOOS1_THIS_CALL,PARSER_EXCEPTION
// JOOS2: AMBIGUOUS_OVERLOADING
// JAVAC:UNKNOWN
/**
 * Typecheck: 
 * - (Joos 1) Check that any method or constructor invocation resolves
 *   to a unique method with a type signature matching exactly the
 *   static types of the argument expressions.
 * - (Joos 2) Check that any method or constructor invocation resolves
 *   to a uniquely closest matching method or constructor (15.12.2).  
 */
public class Je_16_ClosestMatch_Constructor_NoClosestMatch_This{

    public Je_16_ClosestMatch_Constructor_NoClosestMatch_This(){
	this ((short)1, (short)2);
    }

    public Je_16_ClosestMatch_Constructor_NoClosestMatch_This(int a, short b) {}
    
    public Je_16_ClosestMatch_Constructor_NoClosestMatch_This(short a, int b) {}
    
    public static int test() {
	return 123;
    }
}
