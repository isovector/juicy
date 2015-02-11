// JOOS1:PARSER_WEEDER,JOOS1_INTERFACE,PARSER_EXCEPTION
// JOOS2:TYPE_LINKING,SINGLE_TYPE_IMPORT_CLASH_WITH_CLASS
// JAVAC:UNKNOWN
/**
 * Parser/weeder:
 * - (Joos 1) No interfaces allowed
 * Hierarchy check:
 * - (Joos 2) Check that no single-type-import declarations clash with
 * the class (Joos 2: or interface) defined in the same file.  
 */
public abstract class Main implements List {

    public Main() { }

    public static int test() { 
	return 123; 
    }

}
