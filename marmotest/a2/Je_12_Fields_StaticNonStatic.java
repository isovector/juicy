// JOOS1: PARSER_WEEDER,PARSER_EXCEPTION,JOOS1_STATIC_FIELD_DECLARATION
// JOOS2: ENVIRONMENTS,DUPLICATE_FIELD
// JAVAC:UNKNOWN
/**
 * Parser/weeder: 
 *- (Joos 1) No static field declarations allowed
 * Environments: 
 * - (Joos 2) Check that no two fields in the same class
 * have the same name.
 */
public class Je_12_Fields_StaticNonStatic {

    public int a = 2;
    public static int a = 2;
    
    public Je_12_Fields_StaticNonStatic() {
    }
    
    public static int test() {
	return 123;
    }
}
