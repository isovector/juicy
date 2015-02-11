// PARSER_WEEDER,CODE_GENERATION
/**
 * Parser/weeder:
 * - Tests the precedence of instanceof
 */
public class J1_1_Instanceof_OfAdditiveExpression{

    public J1_1_Instanceof_OfAdditiveExpression(){}

    public static int test(){
	String a = "123";
	boolean b = a + 3 instanceof String;

	if (b){
	    return Integer.parseInt(a);
	}
	else {
	    return -1;
	}
    }

}
