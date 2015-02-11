// JOOS1:PARSER_WEEDER,VOID_TYPE_NOT_RETURN_TYPE,VOID_TYPE_INSTANCEOF,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,VOID_TYPE_NOT_RETURN_TYPE,VOID_TYPE_INSTANCEOF,PARSER_EXCEPTION
// JAVAC:UNKNOWN
// 
/**
 * Parser/weeder:
 * - A PType node which is not the return type of a method must not be
 * an AVoidType
 */
public class Je_1_InstanceOf_Void {

    public Je_1_InstanceOf_Void() {}

    public static int test() { 
	return 123; 
    }

    public boolean foo(Object o) {
	return o instanceof void;
    }
}
