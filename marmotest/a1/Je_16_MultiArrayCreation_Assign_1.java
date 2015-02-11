// JOOS1: PARSER_WEEDER,JOOS1_MULTI_ARRAY,PARSER_EXCEPTION
// JOOS2: TYPE_CHECKING,ASSIGN_TYPE
// JAVAC:UNKNOWN
/**
 * Parser/weeder:
 * - (Joos 1) No multidimensional array creation expressions allowed.
 * - (Joos 2) int[][] is not assignable to int
 */
public class Je_16_MultiArrayCreation_Assign_1{
    
    public Je_16_MultiArrayCreation_Assign_1(){}
    
    public static int test(){
	int a = new int[5][2];
	return 120;
    }
}
