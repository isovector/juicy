// JOOS1: PARSER_WEEDER,PARSER_EXCEPTION,JOOS1_THROW
// JOOS2: REACHABILITY,UNREACHABLE_STATEMENT
// JAVAC:UNKNOWN
/**
 * Parser/weeder:
 * - (Joos 1) Throw statements not allowed
 * Reachability:
 * - (Joos 2) Check that all statements (including empty statements
 * and empty blocks) are reachable.
 */
public class Je_17_Unreachable_AfterThrow{

    public Je_17_Unreachable_AfterThrow(){}

    public static int test(){
	throw new RuntimeException();
	return 123;
    }
}
