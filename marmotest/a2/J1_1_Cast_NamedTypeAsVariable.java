// ENVIRONMENTS
/**
 * Environment:
 * - Variable java.lang.Object not declared
 */
public class J1_1_Cast_NamedTypeAsVariable {

    public J1_1_Cast_NamedTypeAsVariable() {}

    public static int test() {
	int Object = 165;
	int x = (Object)-42;
	return x;
    }

}
