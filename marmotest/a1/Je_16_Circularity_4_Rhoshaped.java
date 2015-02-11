// JOOS1: PARSER_WEEDER,JOOS1_THIS_CALL,PARSER_EXCEPTION
// JOOS2: TYPE_CHECKING,CIRCULAR_CONSTRUCTOR_INVOCATION
// JAVAC:UNKNOWN
/**
 * Parser/weeder:
 * - (Joos 1) No explicit super or this statements allowed
 * Typecheck:
 * - (Joos 2) Check that constructors are not invoking each other
 * circularly using explicit this constructor invocation statements.
 */
public class Je_16_Circularity_4_Rhoshaped {
	public Je_16_Circularity_4_Rhoshaped() {
		this(1);
	}
	public Je_16_Circularity_4_Rhoshaped(int x) {
		this(1,2);
	}
	public Je_16_Circularity_4_Rhoshaped(int x, int y) {
		this(1);
	}
	public static int test() {
		return 123;
	}
}
