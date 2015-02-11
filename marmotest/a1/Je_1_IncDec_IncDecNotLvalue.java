// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:UNKNOWN
// 
/**
 * Parser/weeder:
 * - (Joos 1) Increment and decrement expressions not allowed
 * - (Joos 2) Post increment must only be used on lvalues
 */
public class Je_1_IncDec_IncDecNotLvalue {
	
    public Je_1_IncDec_IncDecNotLvalue() {}
    
    public static int test() {
	int i = 5;
	--i++;
	return 123;
    }

}
