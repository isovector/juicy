// HIERARCHY
/* Hierarchy:
 * class CompA {
 *	int compareTo(Object)
 * }
 * class CompB extends CompA implements Comparable {
 *	int compareTo(Object)
 * }
 */
 public class Main {
    public Main() {}
    public static int test() {
	CompA x = new CompB();
	return x.compareTo((Object)null);
    }
}
