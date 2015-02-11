// JOOS1:HIERARCHY,CLASS_MUST_BE_ABSTRACT
// JOOS2:HIERARCHY,CLASS_MUST_BE_ABSTRACT
// JAVAC:UNKNOWN
// 
/**
 * Hierarchy check:

 * - A class that has (declares or inherits) any abstract methods must
 * be abstract (8.1.1.1). (Method addAll(Collection) from Set not
 * implemented) 
 */
import java.util.*;

public class Main implements SortedSet{
    public Main(){}

    public Comparator comparator(){
	return null;
    }

    public Object first(){
	return null;
    }

    public SortedSet headSet(Object toElement){
	return null;
    }

    public Object last(){
	return null;
    }

    public SortedSet subSet(Object fromElement, Object toElement){
	return null;
    }

    public SortedSet tailSet(Object fromElement){
	return null;
    }

    public boolean add(Object o){
	return false;
    }

    public void clear(){}

    public boolean contains(Object o){
	return false;
    }

    public boolean containsAll(Collection c){
	return false;
    }

    public boolean equals(Object o){
	return false;
    }

    public int hashCode(){
	return 0;
    }

    public boolean isEmpty(){
	return false;
    }

    public Iterator iterator(){
	return null;
    }

    public boolean remove(Object o){
	return false;
    }

    public boolean removeAll(Collection c){
	return false;
    }

    public boolean retainAll(Collection c){
	return false;
    }

    public int size(){
	return 0;
    }

    public Object[] toArray(){
	return null;
    }

    public Object[] toArray(Object[] a){
	return null;
    }

    public static int test(){
	return 123;
    }
}
