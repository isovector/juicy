//PARSER_WEEDER
public class J1_1_Cast_MultipleReferenceArray {
	public J1_1_Cast_MultipleReferenceArray() {}
	
	public static int test() {
		Object a = null;
		a = (Object)(int[])(Object)(Integer[])a;
		return 123;
	}
}
