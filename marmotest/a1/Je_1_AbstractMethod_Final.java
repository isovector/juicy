// JOOS1:PARSER_WEEDER,ABSTRACT_METHOD_FINAL_OR_STATIC,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,ABSTRACT_METHOD_FINAL_OR_STATIC,PARSER_EXCEPTION
// JAVAC:UNKNOWN
/**
 * Parser/weeder:
 * - An abstract method cannot be static or final.
 */
public abstract class Je_1_AbstractMethod_Final {

    public Je_1_AbstractMethod_Final() { }

    public static int test() { 
	return 123; 
    }

    public abstract final void foo();
}
