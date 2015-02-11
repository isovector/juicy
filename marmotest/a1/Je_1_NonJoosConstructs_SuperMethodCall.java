// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:
/**
 * Parser/weeder:
 * - Super method calls not allowed in joos
 */
public class Je_1_NonJoosConstructs_SuperMethodCall{

    public Je_1_NonJoosConstructs_SuperMethodCall(){}

    public String foo(){
	return super.toString();
    }

    public static int test(){
	return 123;
    }

}
