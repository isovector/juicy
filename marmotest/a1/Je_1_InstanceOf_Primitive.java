// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION,TYPE_CHECKING,INVALID_INSTANCEOF
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION,TYPE_CHECKING,INVALID_INSTANCEOF
// JAVAC:UNKNOWN
// 
public class Je_1_InstanceOf_Primitive {
    /* Parser+Weeder => instanceof is not allowed on primitive types */
    public Je_1_InstanceOf_Primitive() {}
    
    public static int test() {
	int a = 0;
	if (a instanceof int) return 42;
	return 123;
    }
}
