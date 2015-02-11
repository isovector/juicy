// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION,SYNTAX_ERROR
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION,SYNTAX_ERROR
// JAVAC:UNKNOWN
public class Je_1_CastToArrayLvalue {
	public Je_1_CastToArrayLvalue() {}

	public static int test() {
		int[] ia = new int[5];
		int i = (ia[5]) ia;
		return 123;
	}

	public static void main(String[] args) {
		System.out.println(Je_1_CastToArrayLvalue.test());
	}
}
