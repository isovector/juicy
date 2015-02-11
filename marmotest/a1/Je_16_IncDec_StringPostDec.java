// JOOS1: PARSER_WEEDER, JOOS1_INC_DEC,PARSER_EXCEPTION
// JOOS2: TYPE_CHECKING, NON_NUMERIC_INC_DEC
// JAVAC:UNKNOWN
/**
 * Parser/Weeder:
 * - (Joos 1) Increment and decrement operators not allowed
 * Typecheck:
 * - (Joos 2) Increment operator only allowed on numeric types
 */
public class Je_16_IncDec_StringPostDec{
	
    public Je_16_IncDec_StringPostDec() {}
    
    public static int test() {
	String d = "1";
	d--;
	return 123;
    }
}
