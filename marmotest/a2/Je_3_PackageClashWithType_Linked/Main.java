//TYPE_LINKING
//JOOS1:PACKAGE_CLASH_WITH_TYPE
//JOOS2:PACKAGE_CLASH_WITH_TYPE
//JAVAC:UNKNOWN

/**
 * TypeLinking:
 * Check that no package names or prefixes hereof (consisting of whole
 * identifiers) of declared packages, single-type-import declarations
 * or used import-on-demand declarations resolve to qualified types,
 * i.e. types not in the default package.
 *
 * The declared class javax.swing.tree clashes with the package
 * of the same name because the import on-demand declaration of 
 * javax.swing.tree is used for resolving TreeNode.
 */
import javax.swing.tree.*;

public class Main {
	public Main() {}
	
	public static int test() {
		TreeNode node = null;
		return 123;
	}
}
