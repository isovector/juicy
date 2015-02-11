// JOOS1:TYPE_LINKING,SINGLE_TYPE_IMPORT_CLASH_WITH_CLASS
// JOOS2:TYPE_LINKING,SINGLE_TYPE_IMPORT_CLASH_WITH_CLASS
// JAVAC:UNKNOWN
// 
/**
 * Typelinking:
 * - Check that no single-type-import declarations clash with the
 * class (Joos 2: or interface) defined in the same file. (see List.java)
 */
public class Main {
    
    public Main() {}

    public static int test() {
	return 123;
    }
}
