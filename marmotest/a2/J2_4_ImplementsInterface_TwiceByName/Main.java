// HIERARCHY
// JOOS1: PARSER_WEEDER,JOOS1_INTERFACE,PARSER_EXCEPTION
/* Hierarchy:
 * An interface must not be mentioned more than once in the same implements 
 * clause of a class (8.1.4, simple constraint 3).
 */

public class Main implements Cloneable, foo.Cloneable {
	public Main() {}
	
	public static int test() {
		return 123;
	}
}
