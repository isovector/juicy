// PARSER_WEEDER
public class J1_constructorparameter {

    protected int i;

    public J1_constructorparameter(int i) {
	this.i = i;
    }

    public static int test() {
        return new J1_constructorparameter(123).i;
    }

}
