// TYPE_LINKING
/* TypeLinking:
 * Check that no prefixes (consisting of whole identifiers) of fully qualified
 * types themselves resolve to types.
 * 
 * Prefix Main of Main.B.A does not resolve to a type, since the Main class is
 * not visible from A. (see A.java)
 */
public class Main {
    public Main() {}
    
    public static int test() {
	return 123;
    }
}
