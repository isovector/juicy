// HIERARCHY
public class J1_inherited_hashcode {
    public J1_inherited_hashcode() {}
    public static int test() {
	return 123 + new J1_inherited_hashcode().foo();
    }
    public int foo() {
	return hashCode() - this.hashCode();
    }
}
