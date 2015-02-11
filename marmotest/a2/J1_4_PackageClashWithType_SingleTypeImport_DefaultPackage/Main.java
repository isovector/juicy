//TYPE_LINKING

/**
 * TypeLinking:
 * Check that no package names or prefixes hereof (consisting of whole
 * identifiers) of declared packages, single-type-import declarations
 * or used import-on-demand declarations resolve to qualified types,
 * i.e. types not in the default package.
 *
 * The prefix 'foo' of the single-type import declaration foo.bar does
 * not clash with class foo because it is class foo is in the default package.
 */
import foo.bar;

public class Main {
	public Main() {}
	
	public static int test() {
		return bar.method();
	}
}
