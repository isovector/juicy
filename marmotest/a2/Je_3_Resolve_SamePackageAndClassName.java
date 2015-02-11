// JOOS1:TYPE_LINKING,PREFIX_RESOLVES_TO_TYPE
// JOOS2:TYPE_LINKING,PREFIX_RESOLVES_TO_TYPE
// JAVAC:UNKNOWN
// 
/**
 * Typelinking:
 * - Check that no prefixes (consisting of whole identifiers) of fully
 * qualified types themselves resolve to types.
 */
package Je_3_Resolve_SamePackageAndClassName;

public class Je_3_Resolve_SamePackageAndClassName {

    public Je_3_Resolve_SamePackageAndClassName() {}

    public void test() {
	new Je_3_Resolve_SamePackageAndClassName.Je_3_Resolve_SamePackageAndClassName().test();
    }

}
