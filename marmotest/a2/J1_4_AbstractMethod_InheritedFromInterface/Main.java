// HIERARCHY
/**
 * Hierarchy check:
 * - A class that has (declares or inherits) any abstract methods must
 * be abstract (8.1.1.1). (method equals(Object) inherited from superclass)
 */
import java.util.Comparator;

public class Main implements Comparator{

    public Main(){
    }

    public int compare(Object o1, Object o2){
	return o1.hashCode() - o2.hashCode() + 123;
    }

    public static int test(){
	Main object = new Main();
	return object.compare((Object)object, (Object)object);
    }

}
