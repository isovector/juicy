// JOOS1: PARSER_WEEDER,JOOS1_MULTI_ARRAY,PARSER_EXCEPTION
// JOOS2: TYPE_CHECKING,ASSIGN_TYPE
// JAVAC:UNKNOWN
/* 
 * Calls a static method without naming the class. 
 */

public class Je_16_ClosestMatch_Array {
    public Je_16_ClosestMatch_Array() {}
    
    public static int method(Object[] e) { return 123; }
    public static void method(Cloneable[][] e) {}
    public static int method(Cloneable[] e) { return 123; }
    
    public static int test() {
	Cloneable[][] c = null;
	return method(c);
    }
}
