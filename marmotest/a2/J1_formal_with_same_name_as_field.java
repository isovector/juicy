// ENVIRONMENTS,DISAMBIGUATION
public class J1_formal_with_same_name_as_field{

    protected String s = "Hello World!";

    public J1_formal_with_same_name_as_field(){}

    public J1_formal_with_same_name_as_field(String s){}

    public int testerMethod(String s){

	    return 123;
    }

    public static int test(){

	J1_formal_with_same_name_as_field j = new J1_formal_with_same_name_as_field("Hello Wrold!");
	return j.testerMethod("Hello World!");
    }

}
