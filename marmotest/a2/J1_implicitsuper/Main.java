// HIERARCHY
/* 
 * Foo extends Bar, Bar declares the int field 'field',
 * which is inherited by Foo.
 */
public class Main {
	public Main() { }
	public static int test() {
		Foo foo = new Foo();
		return foo.field;
	}
}
 
