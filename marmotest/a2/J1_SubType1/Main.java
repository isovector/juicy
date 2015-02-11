// HIERARCHY
/* Hierarchy:
 * A {
 *	int testA()
 * }
 * Main extends A {
 *	int testA()
 * }
 */
public class Main extends A {

    public Main(){}

    public static int test() {
	
	A a = new Main();

	return a.testA();
	
    }

    public int testA(){
	return 123;
    }
}
