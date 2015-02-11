//TYPE_LINKING

/**
 * Same package precedes on-demand import in type-linking =>
 * On-demand imports are not ambiguous.
 */
import foo.Foo;

public class Main {
	public Main() {}
	
	public static int test() {
		Foo foo = new Foo();
		return foo.method();
	}
}
