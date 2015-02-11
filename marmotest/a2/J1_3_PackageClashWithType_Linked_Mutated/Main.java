//TYPE_LINKING

/**
 * TypeLinking:
 * Check that no package names or prefixes hereof (consisting of whole
 * identifiers) of declared packages, single-type-import declarations
 * or used import-on-demand declarations resolve to qualified types,
 * i.e. types not in the default package.
 */
import javax.swing.tree.*;

public class Main {
	public Main() {}
	
	public static int test() {
		TreeNode node = null;
		return 123;
	}
}
