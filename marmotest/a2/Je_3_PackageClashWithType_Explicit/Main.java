//TYPE_LINKING
//JOOS1:PREFIX_RESOLVES_TO_TYPE
//JOOS2:PREFIX_RESOLVES_TO_TYPE
//JAVAC:UNKNOWN


/**
 * TypeLinking:
 * Check that no prefixes (consisting of whole identifiers) of
 * fully qualified types themselves resolve to types.
 *
 * The prefix 'javax.swing.tree' of the fully qualified type
 * javax.swing.tree.TreeNode is itself a type.
 */

public class Main {
	public Main() {}
	
	public static int test() {
		javax.swing.tree.TreeNode node = null;
		return 123;
	}
}
