// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:UNKNOWN
// 
/**
 * Parser/weeder:
 * - (Joos 1) Multidimensional array types not allowed
 * - (Joos 2) Dimensions not allowed in array types.
 */
public class Je_1_MultiArrayTypes_Dimensions {

    public Je_1_MultiArrayTypes_Dimensions() {}

    public static int test() {
        int[][42] a = new int[2][2];
        return 123;
    }
}
