// JOOS1:PARSER_WEEDER,VOID_TYPE_NOT_RETURN_TYPE,VOID_TYPE_FIELD,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,VOID_TYPE_NOT_RETURN_TYPE,VOID_TYPE_FIELD,PARSER_EXCEPTION
// JAVAC:UNKNOWN
// 
/**
 * Parser/weeder:
 * - Void type not allowed for fields
 */
public class Je_1_VoidType_Field {

    public void foo;

    public Je_1_VoidType_Field() {}

    public static int test() {
	return 123;
    }

}
