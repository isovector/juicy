// PARSER_WEEDER
// JOOS1: JOOS1_INTERFACE,PARSER_EXCEPTION
// JOOS2: INTERFACE_METHOD_WITH_BODY,PARSER_EXCEPTION
// JAVAC: UNKNOWN
/**
 * Parser/weeder:
 * - (Joos 1) No interfaces allowed
 * - (Joos 2) An interface method must not have a body
 */
public interface Je_1_Interface_MethodBody {
	
    public int test() {
	return 123;
    }

}
