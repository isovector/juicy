// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION,ABSTRACT_METHOD_FINAL_OR_STATIC
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION,ABSTRACT_METHOD_FINAL_OR_STATIC
// JAVAC:UNKNOWN
/**
 * Parser/weeder:
 * - An abstract method cannot be static or final.
 */
public abstract class Je_1_AbstractMethod_Static {

    public Je_1_AbstractMethod_Static() { }

    public static int test() { 
	return 123; 
    }

    public abstract static void foo();
}
