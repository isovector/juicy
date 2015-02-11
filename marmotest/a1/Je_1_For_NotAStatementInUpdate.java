//JOOS1:PARSER_WEEDER,PARSER_EXCEPTION,SYNTAX_ERROR
//JOOS2:PARSER_WEEDER,PARSER_EXCEPTION,SYNTAX_ERROR
//JAVAC:UNKNOWN

public class Je_1_For_NotAStatementInUpdate {
	public Je_1_For_NotAStatementInUpdate() {}
	public static int test() {
		for (boolean b=false;b;true||true);
		return 123;
	}
}

