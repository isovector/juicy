// JOOS1:PARSER_WEEDER,INVALID_INTEGER
// JOOS2:PARSER_WEEDER,INVALID_INTEGER
// JAVAC:UNKNOWN
// 
/**
 * Parser/weeder:
 * - For each integer literal in the program, .... check that the
 * number is within the legal range for 32-bit signed integers.  
 */
public class Je_1_IntRange_TooBigInt {

    public Je_1_IntRange_TooBigInt(){}
    
    public static int test() {
	return 2147483648 - 2147483525;
    }
}

