// JOOS1:HIERARCHY,CLASS_MUST_BE_ABSTRACT
// JOOS2:HIERARCHY,CLASS_MUST_BE_ABSTRACT
// JAVAC:UNKNOWN
// 
/**
 * Hierarchy check:
 * - A class that has (declares or inherits) any abstract methods must
 * be abstract (8.1.1.1, well-formedness constraint 4).
 */
public class Je_4_AbstractMethod_Declared {

    public Je_4_AbstractMethod_Declared() {}
    
    public abstract int foo();

    public static int test() {
	return 123;
    }
}
