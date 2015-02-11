// PARSER_WEEDER,REACHABILITY
public class J1_ifThenElse {

    public J1_ifThenElse () {

    }

    public static int test() {
	if (true)
	    if (false) {
		return 7;
	    }
	    else {
		return 123;
	    }
	else
	    return 7;
    }

    public static void main(String[] args) {
	System.out.println(J1_ifThenElse.test());
    }
}
