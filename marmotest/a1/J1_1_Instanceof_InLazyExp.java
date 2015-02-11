// PARSER_WEEDER,CODE_GENERATION
/**
 * Parser/weeder:
 * - Tests the priority of instanceof
 */
public class J1_1_Instanceof_InLazyExp{

    public J1_1_Instanceof_InLazyExp(){}

    public static int test(){
	
	boolean b = true;
	boolean e = false;
	Object a = new J1_1_Instanceof_InLazyExp();
	boolean c = e || a instanceof J1_1_Instanceof_InLazyExp;
	boolean d = b && a instanceof J1_1_Instanceof_InLazyExp;

	if (c && d){
	    return 123;
	}
	else {
	    return 12378;
	}
    }

}
