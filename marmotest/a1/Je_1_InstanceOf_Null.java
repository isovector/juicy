// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:UNKNOWN
// 
/**
 * Parser/weeder:
 * - null value is not a type
 */
public class Je_1_InstanceOf_Null {

    public Je_1_InstanceOf_Null() { }

    public static int test() {
	Object o=new Object();
	if (o instanceof null)
	    return 100;
	return 123;
    }
}
