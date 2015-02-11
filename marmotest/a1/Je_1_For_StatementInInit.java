//JOOS1:PARSER_WEEDER,PARSER_EXCEPTION,SYNTAX_ERROR
//JOOS2:PARSER_WEEDER,PARSER_EXCEPTION,SYNTAX_ERROR
//JAVAC:UNKNOWN

public class Je_1_For_StatementInInit {
	public Je_1_For_StatementInInit() {}
	
	public static int test() {
		int a = 1;
		int i = 1;
		for (if (a == 2) i = 2; i < 2 ; i = i + 1) {
			a = 123;
		}
		return a;
	}
}
