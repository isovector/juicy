// PARSER_WEEDER,CODE_GENERATION
public class J1_NamedTypeArray {

    public J1_NamedTypeArray(){}

	public static int test(){
		J1_NamedTypeArray[] a = new J1_NamedTypeArray[5];
		if (a[4] == null) {
			return 123;
		}
		return 444;
	}
}

