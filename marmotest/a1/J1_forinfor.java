// PARSER_WEEDER
public class J1_forinfor {

    public J1_forinfor () {}

    public static int test() {
	int i = 42;
	int j = 117;
	int k = 512;
	for (i=0; i<5; i=i+1)
	    for (j=0; j<10; j=j+1)
		k = i+j;
	return 123;
    }

}
