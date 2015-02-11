//TYPE_LINKING

/**
 * TypeLinking:
 * Check that no package names or prefixes hereof (consisting of whole
 * identifiers) of declared packages, single-type-import declarations
 * or used import-on-demand declarations resolve to qualified types,
 * i.e. types not in the default package.
 *
 * The declared class javax.swing.tree does not clash with the package
 * of the same name because javax.swing.tree is not refered.
 */
public class Main {
	public Main() {}
	
	public static int test() {
		return 123;
	}
}
