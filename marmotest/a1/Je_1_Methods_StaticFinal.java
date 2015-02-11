// JOOS1:PARSER_WEEDER,STATIC_FINAL_METHOD,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,STATIC_FINAL_METHOD,PARSER_EXCEPTION
// JAVAC:
/**
 * Parser/weeder:
 * - A static method cannot be final.
 */
public class Je_1_Methods_StaticFinal {

    public Je_1_Methods_StaticFinal() {}

    public static final int test(){
	return 123;
    }

}
