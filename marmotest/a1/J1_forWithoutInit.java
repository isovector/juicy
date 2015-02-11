// PARSER_WEEDER
public class J1_forWithoutInit {

    public J1_forWithoutInit () {}

    public static int test() {
	int i = 1;
	int j = 1;
	for (; i<6; i=i+1) {
	    System.out.println(i);
	    j = i * j;
	}
        return j+3;
    }

    public static void main(String[] args) {
	System.out.println("j: "+J1_forWithoutInit.test());
    }

}
