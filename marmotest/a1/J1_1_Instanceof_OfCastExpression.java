// PARSER_WEEDER,CODE_GENERATION
/**
 * Parser/weeder:
 * Tests the precedence relation between casts and instanceof
 */
public class J1_1_Instanceof_OfCastExpression{

    public J1_1_Instanceof_OfCastExpression(){}

    public static int test(){
	J1_1_Instanceof_OfCastExpression o = new J1_1_Instanceof_OfCastExpression();
	boolean b = (Object) o instanceof J1_1_Instanceof_OfCastExpression;
	if (b){
	    return 123;
	}
	else {
	    return -1;
	}
    }

}
