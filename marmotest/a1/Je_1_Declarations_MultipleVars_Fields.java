// PARSER_WEEDER
// JOOS1:PARSER_EXCEPTION
// JOOS2:PARSER_EXCEPTION
// JAVAC:
/**
 * Parser/weeder:
 * - Multiple variables in same declaration not allowed. 
 */
public class Je_1_Declarations_MultipleVars_Fields {

    public int x,y,z;
    
    public Je_1_Declarations_MultipleVars_Fields() {}
    
    public static int test() {
	return 123;
    }
}
