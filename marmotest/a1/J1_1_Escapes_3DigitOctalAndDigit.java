// PARSER_WEEDER
/**
 * Parser/weeder
 * - Test of octal escape decoding
 */
public class J1_1_Escapes_3DigitOctalAndDigit{

    public J1_1_Escapes_3DigitOctalAndDigit(){}

    public static int test(){
	String s = "\1674";
	return s.charAt(0) + s.charAt(1) - '0';
    }

}
