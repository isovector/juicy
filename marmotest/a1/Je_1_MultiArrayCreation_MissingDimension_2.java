// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:UNKNOWN
// 
/**
 * Parser/weeder:
 * - (Joos 1) No multidimensional array creation expressions allowed.
 * - (Joos 2) At least one dimension must be given in multidimensional
 * array creation expressions.  
 */
public class Je_1_MultiArrayCreation_MissingDimension_2 {

    public Je_1_MultiArrayCreation_MissingDimension_2() {}

    public static int test() {
        int[][] a = new int[][];
        return 123;
    }
}
