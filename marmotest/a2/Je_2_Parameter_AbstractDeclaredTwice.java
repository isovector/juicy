//JOOS1:ENVIRONMENTS,DUPLICATE_VARIABLE
//JOOS2:ENVIRONMENTS,DUPLICATE_VARIABLE
//JAVAC:UNKNOWN
public abstract class Je_2_Parameter_AbstractDeclaredTwice {
	public Je_2_Parameter_AbstractDeclaredTwice() {}

	public abstract void foo(Object x, java.lang.Object x);

	public static int test() {
		return 123;
	}
}
