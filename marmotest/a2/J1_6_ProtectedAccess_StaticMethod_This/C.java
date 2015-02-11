/* TypeChecking:
 * 
 * Test for Protected Access
 * 
 * B.B extends A.A
 * C.C extends B.B
 * D.D extends C.C
 */

package C;

public class C extends B.B {
    public C() {}

    /* static method access through this class
     * => OK, since C is a subclass of the declaring class A.A (6.6.2.1)
     */ 
    public void staticMethodAccessFromThisClass() {
	C.staticMethod();
    }
}
