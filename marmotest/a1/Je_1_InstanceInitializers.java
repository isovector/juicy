// PARSER_WEEDER
// JOOS1: PARSER_EXCEPTION
// JOOS2: PARSER_EXCEPTION
// JAVAC: 
/**
 * Parser/weeder:
 * - Instance initializers not allowed.
 */
public class Je_1_InstanceInitializers {

    public Je_1_InstanceInitializers() {}

    public int x;

    {
	x = 123; 
    }

    public static int test() {
	return 123;
    }
}
