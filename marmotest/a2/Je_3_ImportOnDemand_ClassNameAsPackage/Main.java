// JOOS1:TYPE_LINKING,NON_EXISTING_PACKAGE
// JOOS2:TYPE_LINKING,NON_EXISTING_PACKAGE
// JAVAC:UNKNOWN
// 
/* TypeLinking:
 * Check that all import-on-demand declarations refer to existing packages.
 * 
 * Main is not a package (see A.java)
 */

public class Main {
    public Main() {}
    
    public static int test() {
	return 123;
    }
}
