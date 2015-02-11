// JOOS1:PARSER_WEEDER,INVALID_INTEGER
// JOOS2:PARSER_WEEDER,INVALID_INTEGER
// JAVAC:UNKNOWN
// 
/**
 * Parser/weeder:
 * - Check that all integer constant values are within the legal range
 * for the int type.
 */
public class Je_1_IntRange_MinusTooBigInt {
    
    public Je_1_IntRange_MinusTooBigInt(){}
    
    public static int test() {
	return 2147483000 - 2147483648 + 771;
    }
}

