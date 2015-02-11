// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:UNKNOWN
// 
/**
 * Parser/weeder:
 * - (Joos 1) No multidimensional array creation expressions allowed
 * - (Joos 2) Multidimenstional array creation expressions must have a
 * base type.
 */
public class Je_1_MultiArrayCreation_NoType {

    public Je_1_MultiArrayCreation_NoType() {}

    public static int test() {
        int[][] a = new [2][2];
        return 123;
    }
}
