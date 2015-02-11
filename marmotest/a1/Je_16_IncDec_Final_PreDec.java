// JOOS1: PARSER_WEEDER, JOOS1_INC_DEC,PARSER_EXCEPTION
// JOOS2: TYPE_CHECKING, ASSIGN_TO_FINAL_FIELD
// JAVAC:UNKNOWN
/**
 * Parser/weeder:
 * - (Joos 1) Increment and decrement operators not allowed
 * Typecheck:
 * - (Joos 2) A final field must not be assigned to.
 */
public class Je_16_IncDec_Final_PreDec {

    public Je_16_IncDec_Final_PreDec(){
	--Integer.MAX_VALUE;
    }
    
    public static int test() {
	return 123;
    }

}
