// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
/**
 * Parser/weeder:
 * - Nested types not allowed.
 */
public class Je_1_NonJoosConstructs_NestedTypes {

    public Je_1_NonJoosConstructs_NestedTypes() {}

    public class A {
	
		public A(){}
    }
    
    public static int test() {
    	return 123;
    }
}
