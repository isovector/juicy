// PARSER_WEEDER
// JOOS1:PARSER_EXCEPTION
// JOOS2:PARSER_EXCEPTION
// JAVAC:
/**
 * Parser/weeder:
 * - Array data expressions not allowed
 */
public class Je_1_Array_Data_Empty {

    public Je_1_Array_Data_Empty() {}

    public static int test() {
	int[] x = {};
	return 123;
    }
}

