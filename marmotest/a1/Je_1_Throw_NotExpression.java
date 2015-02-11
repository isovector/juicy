// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:UNKNOWN
// 
/**
 * Parser/weeder:
 * - (Joos 1) No throw statements allowed
 * - (Joos 2) Throw statements not allowed as expressions.
 */
public class Je_1_Throw_NotExpression {

    public Je_1_Throw_NotExpression() {}
    
    public static int test() {
	Exception a = throw new RuntimeException();
	return 123;
    }
}
