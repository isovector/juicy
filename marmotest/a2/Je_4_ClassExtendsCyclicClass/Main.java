// JOOS1:HIERARCHY,CIRCULAR_INHERITANCE
// JOOS2:HIERARCHY,CIRCULAR_INHERITANCE
// JAVAC:UNKNOWN
// 
/* HierarchyCheck:
 * A class or interface must not depend on itself
 * (8.1.3, 9.1.2, well-formedness constraint 1).
 * 
 * A extends B 
 * B extends C
 * C extends B
 */

public class Main {
    public Main() {}
    
    public static int test() {
	return 123;
    }
}
