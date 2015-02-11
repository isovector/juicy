// HIERARCHY
/*
 * Bar declares the public field 'j'.
 */
public class Main {
    public Main() {}

   public static void main(String[] args) {
       //	System.out.println(test());
    }

    public static int test() {
	Bar b = new Bar();
	int i = b.j;
	return i;
    }
}
