// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION,SYNTAX_ERROR
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION,SYNTAX_ERROR
// JAVAC:UNKNOWN
// 
/**
 * Parser/weeder:
 * - Method invocation not allowed as type in cast.
 */
public class Je_1_Cast_ToMethodInvoke {

    public Je_1_Cast_ToMethodInvoke () {}

    public Je_1_Cast_ToMethodInvoke foo() {
	return null;
    }

    public static int test() {
	Je_1_Cast_ToMethodInvoke x = null;
	Object y = (x.foo()) x;
        return 123;
    }

}
