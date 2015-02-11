// JOOS1:PARSER_WEEDER,INVALID_INTEGER
// JOOS2:PARSER_WEEDER,INVALID_INTEGER
// JAVAC:UNKNOWN
// 
/**
 * Parser/weeder:
 * - For each integer ... Check that the number is within
 * the legal range for 32-bit signed integers.
 */
public class Je_1_IntRange_TooBigInt_InInitializer {

    public Je_1_IntRange_TooBigInt_InInitializer () {}

    public static int test() {
       	int i = 2147483648;
        return 123;
    }

}
