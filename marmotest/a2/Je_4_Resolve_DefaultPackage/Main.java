//JOOS1:TYPE_LINKING,PREFIX_RESOLVES_TO_TYPE
//JOOS2:TYPE_LINKING,PREFIX_RESOLVES_TO_TYPE
//JAVAC:UNKNOWN
public class Main {
	public Main() {}
	
	public static int test() {
		foo.bar fb = null;
		return 123;
	}
}
