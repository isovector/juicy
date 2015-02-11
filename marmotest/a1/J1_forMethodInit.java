// PARSER_WEEDER
public class J1_forMethodInit {

    public J1_forMethodInit () {}

    public int foo() {
	return 7;
    }

    public static int test() {
	J1_forMethodInit k = new J1_forMethodInit();
        int i = 1;
	int j = 1;
	for (k.foo();i<6;i=i+1) {
	    j = i * j;
	}
	return j + 3;
    }

    public static void main(String[] args) {
	J1_forMethodInit.test();
    }
}
