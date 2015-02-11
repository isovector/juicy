// JOOS1:HIERARCHY,CIRCULAR_INHERITANCE
// JOOS2:HIERARCHY,CIRCULAR_INHERITANCE
// JAVAC:UNKNOWN
// 
/**
 * Hierarchy:
 * - A class must not depend on itself
 */
public class Main extends foo {

    public Main () {}

    public static int test() {
        return 123;
    }

}
