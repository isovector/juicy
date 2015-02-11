// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:
/**
 * Parser/weeder:
 * - Static initializers not allowed in joos
 */
public class Je_1_NonJoosConstructs_StaticInitializers {

    public Je_1_NonJoosConstructs_StaticInitializers() {}

    static { 
	Je_1_NonJoosConstructs_StaticInitializers.test(); 
    }

    public static int test() {
	return 123;
    }
}
