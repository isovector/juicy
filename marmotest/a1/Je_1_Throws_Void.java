// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:UNKNOWN
// 
/**
 * Parser/weeder:
 * - A PType node which is not the return type of a method must not
 * be an AVoidType.
 */
public class Je_1_Throws_Void{
    
    public Je_1_Throws_Void() throws void {}

    public static int test(){
	return 123;
    }
    
}
