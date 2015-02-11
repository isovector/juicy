// PARSER_WEEDER
// JOOS1:PARSER_EXCEPTION
// JOOS2:PARSER_EXCEPTION
// JAVAC:
/**
 * Parser/weeder:
 * - Array data expressions not allowed
 */
public class Je_1_Array_Data {

    public Je_1_Array_Data() {}

    public static int test() {
	int[] x = { 123,1,2,3 };
	return 123;
    }
}

