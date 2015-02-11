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
 * The package foo.bar of the declared class foo.bar.baz clashes with
 * the type name of the declared class foo.bar. 
 */
public class Main {
	public Main() {}
	
	public static int test() {
		return 123;
	}
}
