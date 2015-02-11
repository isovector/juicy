// JOOS1: PARSER_WEEDER,JOOS1_THROW,PARSER_EXCEPTION
// JOOS2: TYPE_CHECKING,ASSIGN_TYPE
// JAVAC:UNKNOWN
/**
 * Parser/weeder:
 * - (Joos 1) Explicit throw statement not allowed
 * Typecheck:
 * - (Joos 2) Static type of expression in throw statement must be
 * a subtype of java.lang.Throwable.
 */
public class Je_16_Throw_SimpleType{

    public Je_16_Throw_SimpleType(){}

    public static int test() {
	throw 123;
    }

}
