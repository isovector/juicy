//JOOS1:HIERARCHY,PROTECTED_REPLACE_PUBLIC
//JOOS2:HIERARCHY,PROTECTED_REPLACE_PUBLIC
//JAVAC:UNKNOWN
public class Main extends Foo {
	public Main() {}
	
	// this overrides a public method from Foo
	protected void method() {}
	
	public static int test() {
		return 123;
	}
}
