// PARSER_WEEDER
// JOOS1: LEXER_EXCEPTION
// JOOS2: LEXER_EXCEPTION
// JAVAC: 
/**
 * Parser/weeder:
 * - Labeled statments not allowed
 */
public class Je_1_LabeledStatements {
    
    public Je_1_LabeledStatements(){}

    public static int test() {
	int x = 99;
    loop:
	while (x>0) {
	    x=x-1;
	}
	return 123;
    }
}

