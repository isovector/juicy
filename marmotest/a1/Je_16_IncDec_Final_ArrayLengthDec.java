// JOOS1: PARSER_WEEDER,JOOS1_INC_DEC,PARSER_EXCEPTION
// JOOS2: TYPE_CHECKING,ASSIGN_TO_ARRAY_LENGTH
// JAVAC:UNKNOWN
/**
 * Parser/weeder:
 * - (Joos 1) Increment and decrement operators not allowed
 * Typecheck:
 * - (Joos 2) A final field must not be assigned to. (Array.length is final)
 */
public class Je_16_IncDec_Final_ArrayLengthDec {

    public Je_16_IncDec_Final_ArrayLengthDec () {}

    public static int test() {
	int[] a = new int[7];
	--a.length;
        return 123;
    }

}
