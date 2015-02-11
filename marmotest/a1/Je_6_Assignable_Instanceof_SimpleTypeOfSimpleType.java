// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION,TYPE_CHECKING,INVALID_INSTANCEOF
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION,TYPE_CHECKING,INVALID_INSTANCEOF
// JAVAC:UNKNOWN
// 
/**
 * Typecheck:
 * - Cannot check instanceof on int
 */
public class Je_6_Assignable_Instanceof_SimpleTypeOfSimpleType {

    public Je_6_Assignable_Instanceof_SimpleTypeOfSimpleType () {}

    public static int test() {
	boolean b = (7 instanceof int);
        return 123;
    }

}
