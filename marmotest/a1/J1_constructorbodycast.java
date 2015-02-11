// PARSER_WEEDER,RESOURCES,CODE_GENERATION
public class J1_constructorbodycast {

    public int x;

    public J1_constructorbodycast () {
	String String = "Hello";
	for (int i=0; i<10; i=i+1) {
	    String = (String)+(String)"World";
	}
	this.x = String.length()+68;
    }

    public static int test() {
        return new J1_constructorbodycast().x;
    }

}
