// JOOS1: PARSER_WEEDER,JOOS1_THROW,PARSER_EXCEPTION
// JOOS2: TYPE_CHECKING,ILLEGAL_THROWS
// JAVAC:UNKNOWN
/**
 * Parser/weeder:
 * - (Joos 1): No throw statements allowed.
 * Typecheck:
 * - (Joos 2) If the static type of the expression in a throw
 * statement is a checked exception E2, then the current method or
 * constructor must declare an exception E1 in its throws clause such
 * that E2 is a subclass of E1.
 */
public class Je_16_Throw_NoThrows {

    public Je_16_Throw_NoThrows() {}
    
    public static int test() {
	throw new CloneNotSupportedException();
    }
}
