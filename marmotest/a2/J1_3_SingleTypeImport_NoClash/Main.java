// TYPE_LINKING

import a.bc;
import ab.c;

public class Main {
	public Main() {}
	
	public static int test() {
		return bc.test()+c.test();
	}
}
