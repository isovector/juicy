//TYPE_LINKING
/**
 * The package foo exists because of the class foo.bar.Baz.
 */
import foo.*;

public class Main {
	public Main() {}
	
	public static int test() {
		return 123;
	}
}
