// HIERARCHY
/* Hierarchy:
 * JLS 9.2:
 * If an interface has no direct superinterfaces, then the interface implicitly
 * declares a public abstract member method m with signature s, return type r, 
 * and throws clause t corresponding to each public instance method m with 
 * signature s, return type r, and throws clause t declared in Object, 
 * unless a method with the same signature, same return type, and a compatible 
 * throws clause is explicitly declared by the interface. 
 */

public class Main {
	public Main() {}
	
	public String method(java.io.Serializable o) {
		return o.toString();
	}
	
	public static int test() {
		return 123;
	}
}
