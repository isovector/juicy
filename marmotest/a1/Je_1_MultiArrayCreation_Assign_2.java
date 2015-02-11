// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:UNKNOWN
// 
/**
 * Parser/weeder:
 * - (Joos 1) No multidimensional array creation expressions allowed.
 * - (Joos 2) Multidimensional array creation expression is not an
 * lvalue
 */
public class Je_1_MultiArrayCreation_Assign_2{
    
    public Je_1_MultiArrayCreation_Assign_2(){}
    
    public static int test(){
	new int[5][2] = 42;
	return 120;
    }
}

