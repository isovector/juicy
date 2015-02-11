// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:UNKNOWN

/**
 * Parser/weeder:
 * - Cast of left-hand side of assignment not allowed
 */
public class Je_1_Cast_LeftHandSideOfAssignment_1 {

    public Je_1_Cast_LeftHandSideOfAssignment_1() {}

    public static int test() {
	Object o;
	(Object)o=null;
	return 123;
    }


}
