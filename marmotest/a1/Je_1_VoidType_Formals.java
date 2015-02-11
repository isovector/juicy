// JOOS1:PARSER_WEEDER,VOID_TYPE_NOT_RETURN_TYPE,VOID_TYPE_VARIABLE,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,VOID_TYPE_NOT_RETURN_TYPE,VOID_TYPE_VARIABLE,PARSER_EXCEPTION
// JAVAC:UNKNOWN
// 
/**
 * Parser/weeder:
 * - Void type not allowed for formal parameters
 */
public class Je_1_VoidType_Formals {

    public Je_1_VoidType_Formals() {}

    public static int test(void foo) {
	return 123;
    }
}
