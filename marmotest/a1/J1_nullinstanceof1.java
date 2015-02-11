// PARSER_WEEDER,TYPE_CHECKING
public class J1_nullinstanceof1 {
	public J1_nullinstanceof1() { }
	public static int test() {
		if (null instanceof Object)
			return 100;
		else
			return 123;
	}
}
