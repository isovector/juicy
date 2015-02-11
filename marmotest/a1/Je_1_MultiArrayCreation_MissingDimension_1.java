// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION,JOOS1_MULTI_ARRAY
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION,SYNTAX_ERROR
// JAVAC:UNKNOWN
// 
/**
 * Parser/weeder: 
 * - (Joos 1) No multidimensional array creation expressions allowed.
 * - (Joos 2) Missing dimensions in multiple array creation
 * expressions are only allowed from the right end of the
 * dimension sequence.
 */
public class Je_1_MultiArrayCreation_MissingDimension_1 {

    public Je_1_MultiArrayCreation_MissingDimension_1() {}

    public static int test() {
        int[][]a = new int[][2];
        return 123;
    }
}
