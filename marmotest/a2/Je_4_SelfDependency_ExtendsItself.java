// JOOS1:HIERARCHY,CIRCULAR_INHERITANCE
// JOOS2:HIERARCHY,CIRCULAR_INHERITANCE
// JAVAC:UNKNOWN
// 
/**
 * Hierarchy check:
 * - A class or interface must not depend on itself (8.1.3, 9.1.2).
 *   (must not extend itself)
 */
public class Je_4_SelfDependency_ExtendsItself extends Je_4_SelfDependency_ExtendsItself {

	public Je_4_SelfDependency_ExtendsItself() {}
	
	public static int test() {
		return 123;
	}
}
