// PARSER_WEEDER
public class J1_forAllwaysInit {

    public J1_forAllwaysInit () {}

    public int foo() {
	return 123;
    }

    public int bar() {
	int i = 0;
	for (i=foo(); i>123; i=i+1) {}
	return i;
    }

    public static int test() {
	J1_forAllwaysInit j = new J1_forAllwaysInit();
	return j.bar();
    }

}
