// PARSER_WEEDER
public class J1_forMethodUpdate {

    public J1_forMethodUpdate () {}

    public int i;

    public int foo() {
	int j = 1;
	for (i=1; i<6; bar()) {
	    j = j * i;
	}
	return j+3;
    }

    public void bar() {
	i = i + 1;
    }

    public static int test() {
	J1_forMethodUpdate j = new J1_forMethodUpdate();
       	return j.foo();
    }

    public static void main(String[] args) {
	System.out.println(J1_forMethodUpdate.test());
    }
}
