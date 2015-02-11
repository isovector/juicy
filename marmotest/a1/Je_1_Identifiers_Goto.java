// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:UNKNOWN
// 
/**
 * Parser/weeder:
 * - goto must not be used as an identifier
 */
public class Je_1_Identifiers_Goto {

    public Je_1_Identifiers_Goto() {}

    public static int test() {
	int goto=0; 
	return 123;
    }

}
