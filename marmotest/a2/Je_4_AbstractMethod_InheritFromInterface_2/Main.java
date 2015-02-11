// JOOS1:HIERARCHY,CLASS_MUST_BE_ABSTRACT
// JOOS2:HIERARCHY,CLASS_MUST_BE_ABSTRACT
// JAVAC:UNKNOWN
// 
/**
 * Hierarchy check:
 * - A class that has (declares or inherits) any abstract methods must
 * be abstract (8.1.1.1). (method compare(Object, Object) not implemented)
 */
import java.util.Comparator;

public class Main implements Comparator{

    public Main(){
    }

    public boolean equals(Object obj){
	return true;
    }

    public static int test(){
	return 123;
    }

}
