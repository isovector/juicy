// JOOS1:PARSER_WEEDER,INVALID_INTEGER
// JOOS2:PARSER_WEEDER,INVALID_INTEGER
// JAVAC:UNKNOWN
// 
public class Je_1_NegIntTooLow {
    /* Parser+Weeder => too low negative int */
    public Je_1_NegIntTooLow () {}

    public static int test() {
	if (-2147483649 < -1)
	    return 123;
	return 7;
    }

    public static void main(String[] args) {
	System.out.println(Je_1_NegIntTooLow.test());
    }

}
