// PARSER_WEEDER
// JOOS1: PARSER_EXCEPTION
// JOOS2: PARSER_EXCEPTION
// JAVAC:
/**
 * Parser/weeder:
 * - Octal literals not allowed.
 */
public class Je_1_Literals_Octal{

    public Je_1_Literals_Octal(){}

    public static int test(){
	return 0173;
    }
}
