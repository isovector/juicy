// JOOS1:TYPE_LINKING,DISAMBIGUATION,VARIABLE_OR_TYPE_NOT_FOUND
// JOOS2:TYPE_LINKING,DISAMBIGUATION,VARIABLE_OR_TYPE_NOT_FOUND
// JAVAC:UNKNOWN
// 
/**
 * TypeLinking
 * - Tests whether a simple typename is linked to the correct type in
 * the same package, in the case where other packages are also defined
 * within the program.
 * Specifically, the type name Zoo in Test.Foo should not link to Test2.Zoo
 */
public class Main{

    public Main(){}

    public static int test(){
	return Test.Foo.test();
    }

    public static void main(String[] args){
	System.out.println(Main.test());
    }
    
}
