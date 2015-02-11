// JOOS1:PARSER_WEEDER,INVALID_INTEGER
// JOOS2:PARSER_WEEDER,INVALID_INTEGER
// JAVAC:UNKNOWN
// 
/**
 * Parser/weeder:
 * - For each integer literal in the program, .... check that the
 * number is within the legal range for 32-bit signed integers.  
 */
public class Je_1_IntRange_PlusTooBigInt {

    public Je_1_IntRange_PlusTooBigInt(){}
    
    public static int test() {
	return  0 - 2147483525 + 2147483648;
    }
}

