// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:
/**
 * Parser/weeder:
 * - strictftp classes not allowed in joos
 */
public strictfp class Je_1_NonJoosConstructs_Strictftp {

    public Je_1_NonJoosConstructs_Strictftp() {}

    public static int test() {
	return 123;
    }
}

