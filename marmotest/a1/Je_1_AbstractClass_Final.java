// JOOS1:PARSER_WEEDER,ABSTRACT_FINAL_CLASS,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,ABSTRACT_FINAL_CLASS,PARSER_EXCEPTION
// JAVAC:UNKNOWN
/**
 * Parser/weeder:
 * - An abstract class cannot be final
 */
public final abstract class Je_1_AbstractClass_Final {

    public Je_1_AbstractClass_Final() {}

    public static int test() {
	return 123;
    }

}
