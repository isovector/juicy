// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION,ABSTRACT_METHOD_BODY
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION,ABSTRACT_METHOD_BODY
// JAVAC:UNKNOWN
/**
 * Parser/weeder:
 * - An abstract method must not have a body
 */
public abstract class Je_1_AbstractMethod_EmptyBody {

    public Je_1_AbstractMethod_EmptyBody () {}
    
    public abstract int foo() {}

    public static int test() {
        return 123;
    }

}
