// PARSER_WEEDER
// JOOS1:PARSER_EXCEPTION
// JOOS2:PARSER_EXCEPTION
// JAVAC:
/**
 * Parser/weeder:
 * - Array brackets ('[]') are not allowed to occur in the name of a
 *   variable being declared.
 */
public class Je_1_Array_OnVariableNameInDecl {

    public Je_1_Array_OnVariableNameInDecl() { }

    public static int test() {
	int a[];
	return 123;
    }

}
