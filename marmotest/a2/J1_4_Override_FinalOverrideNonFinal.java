// HIERARCHY
/* Hierarchy:
 * A method must not override a final method 
 * (8.4.3.3, well-formedness constraint 9).
 * 
 * This is the opposite case.
 */
public class J1_4_Override_FinalOverrideNonFinal {
	public J1_4_Override_FinalOverrideNonFinal() {}
	
	public final String toString() {
		return "J1_4_Override_FinalOverrideNonFinal";
	}
	
	public static int test() {
		return 123;
	}
}
