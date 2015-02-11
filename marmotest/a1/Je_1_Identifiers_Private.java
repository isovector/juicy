// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:UNKNOWN
// 
/**
 * Parser/weeder:
 * - keyword private must not be used as an identifier
 */
public class Je_1_Identifiers_Private {

    public Je_1_Identifiers_Private() {}

    public static int test() {
	int private = 0;
	return 123;
    }

}
