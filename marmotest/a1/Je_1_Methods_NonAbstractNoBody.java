// JOOS1:PARSER_WEEDER,NON_ABSTRACT_METHOD_BODY,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,NON_ABSTRACT_METHOD_BODY,PARSER_EXCEPTION
// JAVAC:UNKNOWN
// 
/**
 * Parser/weeder:
 * - A non-abstract method must have a body.
 */
public class Je_1_NonAbstractMethod_Body {

    public Je_1_NonAbstractMethod_Body() { }

    public static int test() { 
	return 123; 
    }

    public void foo();

}
