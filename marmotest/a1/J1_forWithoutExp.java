// PARSER_WEEDER
public class J1_forWithoutExp {

    public J1_forWithoutExp () {}

    public static int test() {
	int j = 1;
	for (int i=1; ; i=i+1) {
	    j = i * j;
	    if (i == 5)
		return j+3;
	}
    }

    public static void main(String[] args) {
	System.out.println(J1_forWithoutExp.test());
    }
}
