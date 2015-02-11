//TYPE_LINKING

/**
 * Same package precedes on-demand import in type-linking =>
 * On-demand imports are not ambiguous.
 */
import java.util.*;
import java.awt.*;

public class Main {
	public Main() {}
	
	public static int test() {
		List list = new List();
		return list.method();
	}
}
