// JOOS1:HIERARCHY,CLASS_MUST_BE_ABSTRACT
// JOOS2:HIERARCHY,CLASS_MUST_BE_ABSTRACT
// JAVAC:UNKNOWN
// 
/* Hierarchy:
 * A class that has (declares or inherits) any abstract methods must be abstract
 * (8.1.1.1, well-formedness constraint 4).
 * 
 * A declares m() as non-abstract
 * B extends A and declares m() as abstract
 * C extends B and does not declare m() => m() is still abstract
 */

public class Main {
    public Main() {}
    
    public static int test() {
	C c = new C();
	return c.m();
    }
}
