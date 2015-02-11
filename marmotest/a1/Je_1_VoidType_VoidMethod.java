// JOOS1:PARSER_WEEDER,VOID_TYPE_NOT_RETURN_TYPE,VOID_TYPE_VARIABLE,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,VOID_TYPE_NOT_RETURN_TYPE,VOID_TYPE_VARIABLE,PARSER_EXCEPTION
// JAVAC:UNKNOWN
// 
/**
 * Parser/weeder:
 * - Void type not allowed for formal parameters
 */
public class Je_1_VoidType_VoidMethod {
	public Je_1_VoidType_VoidMethod() {
		
	}
	
	public void method(void i) {
	}
	
	public static int test() {
		return 123;
	}
}
