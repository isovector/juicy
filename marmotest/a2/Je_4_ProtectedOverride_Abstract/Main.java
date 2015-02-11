//JOOS1:HIERARCHY,PROTECTED_REPLACE_PUBLIC
//JOOS2:HIERARCHY,PROTECTED_REPLACE_PUBLIC
//JAVAC:UNKNOWN
public abstract class Main extends Foo {
	public Main() {}
	
	// this overrides a public method from Foo
	protected abstract void method();
	
	public static int test() {
		return 123;
	}
}
