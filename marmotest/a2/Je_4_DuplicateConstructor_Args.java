// JOOS1:HIERARCHY,DUPLICATE_CONSTRUCTOR
// JOOS2:HIERARCHY,DUPLICATE_CONSTRUCTOR
// JAVAC:UNKNOWN
// 
/**
 * Hierarchy:
 * - A class must not declare two constructors with the same parameter
 * types (8.8.2, simple constraint 5).
 */
public class Je_4_DuplicateConstructor_Args {

    public Je_4_DuplicateConstructor_Args(int foo) { }

    public Je_4_DuplicateConstructor_Args(int bar) { }

    public static int test() { 
	return 123; 
    }
}
