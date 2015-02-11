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
import java.io.FileNotFoundException;

public class Je_16_Throw_ThrowsNotSuperclass{

    public Je_16_Throw_ThrowsNotSuperclass(){}

    public static int test(){
	return 42;
    }

    public void raiser(int n) throws CloneNotSupportedException {
	throw new FileNotFoundException();
    }
}
