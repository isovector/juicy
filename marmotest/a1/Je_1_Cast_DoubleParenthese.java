//JOOS1:PARSER_WEEDER,PARSER_EXCEPTION,SYNTAX_ERROR
//JOOS2:PARSER_WEEDER,PARSER_EXCEPTION,SYNTAX_ERROR
//JAVAC:UNKNOWN
public class Je_1_Cast_DoubleParenthese {
	public Je_1_Cast_DoubleParenthese() {}
	
	public static int test() {
		Object o = ((Object))null;
		return 123;
	}
}
