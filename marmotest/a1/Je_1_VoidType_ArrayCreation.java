// JOOS1:PARSER_WEEDER,VOID_TYPE_NOT_RETURN_TYPE,VOID_TYPE_ARRAY,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,VOID_TYPE_NOT_RETURN_TYPE,VOID_TYPE_ARRAY,PARSER_EXCEPTION
// JAVAC:UNKNOWN
// 
/**
 * Parser/weeder:
 * - A PType node which is not the return type of a method must not be
 * an AVoidType 
 */
public class Je_1_VoidType_ArrayCreation {
    
    public Je_1_VoidType_ArrayCreation(){}
    
    public static int test(){
	Object a = new void[5];
	return 123;
    }
}

