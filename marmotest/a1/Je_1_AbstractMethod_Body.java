// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION,ABSTRACT_METHOD_BODY
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION,ABSTRACT_METHOD_BODY
// JAVAC:UNKNOWN
/**
 * Parser/weeder:
 * - An abstract method must not have a body
 */
public class Je_1_AbstractMethod_Body {

    public Je_1_AbstractMethod_Body() { }

    public static int test() { 
	return 123;
    }

    public abstract int foo() {
	return 123;
    }
}
