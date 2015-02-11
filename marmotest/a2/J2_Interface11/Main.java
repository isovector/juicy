// JOOS1: PARSER_WEEDER,PARSER_EXCEPTION,JOOS1_INTERFACE
// JOOS2: HIERARCHY

public class Main implements Foo, Bar, Baz {

	public Main(){}
	
	public static int test() {
		return 123;
	}
	
	public int baz(String i) {
		return 123;
	}

	public int baz(Object o) {
		return 123;
	}
	
	public int baz(String i, String j) {
		return 123;
	}
}
