// HIERARCHY
/* Hierarchy:
 * A protected method must not override a public method
 * (8.4.6.3, well-formedness constraint 7).
 * 
 * This is the opposite case.
 */
public class J1_4_Override_PublicOverridesProtected {
	public J1_4_Override_PublicOverridesProtected() {}
	
	public Object clone() {
		return null;
	}
	
	public static int test() {
		return 123;
	}
}
