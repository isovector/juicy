/* TypeLinking:
 * Check that no prefixes (consisting of whole identifiers) of fully qualified
 * types themselves resolve to types.
 * 
 * Prefix Main of Main.B.A does not resolve to a type, since the Main class is
 * not visible from A. (see A.java)
 */
package Main.B;

public class A {
    public A() {}
    
    public static Main.B.A getInstance() {
	return new Main.B.A();
    }
}
