// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION,SYNTAX_ERROR
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION,SYNTAX_ERROR
// JAVAC:UNKNOWN
public class Je_1_Cast_NonstaticField {
    /* Parser+Weeder => cast to a nonstatic field is not allowed */
    public int value = 123;
    
    public Je_1_Cast_NonstaticField() {}
    
    public static int test() {
	int a = (new Je_1_Cast_NonstaticField().value)123;
	return a;
    }
}
