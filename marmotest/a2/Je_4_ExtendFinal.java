// JOOS1:HIERARCHY,EXTENDS_FINAL_CLASS
// JOOS2:HIERARCHY,EXTENDS_FINAL_CLASS
// JAVAC:UNKNOWN
// 
/**
 * Hierarchy:
 * - A class must not extend a final class (8.1.1.2, 8.1.3, simple
 * constraint 4).
 */
public class Je_4_ExtendFinal extends Integer {

    public Je_4_ExtendFinal() {}

    public static int test() {
	return 123;
    }

}
