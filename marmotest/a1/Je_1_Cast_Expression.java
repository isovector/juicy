// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION,SYNTAX_ERROR
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION,SYNTAX_ERROR
// JAVAC:UNKNOWN

public class Je_1_Cast_Expression {
    	/* Parser+Weeder => cast to an expression is not allowed */
	public Je_1_Cast_Expression() {}
	
	public static int test() {
		int a = 1;
		Object o = (5-a)null;
		return 123;
	}
}
