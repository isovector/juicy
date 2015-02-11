// HIERARCHY
/* Hierarchy:
 *  A class must not declare two constructors with the same parameter types
 */
public class J1_4_Constructor_MatchAsSets {
    
    public J1_4_Constructor_MatchAsSets() {}
    public J1_4_Constructor_MatchAsSets(String a, Object b) {}
    public J1_4_Constructor_MatchAsSets(Object b, String a) {}
    
    public static int test() {
	return 123;
    }
}
