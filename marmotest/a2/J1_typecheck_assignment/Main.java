// HIERARCHY,TYPE_CHECKING
/* TypeChecking: (only works with Java 1.3)
 * 
 * class A {
 *  int f
 * }
 *
 * class B extends A {
 *  int f
 * }
 */
public class Main {

    public Main () {}

    public static int test() {
	A a = new A(42);
	B b = new B(42);
        return (a=b).f;
    }

}
