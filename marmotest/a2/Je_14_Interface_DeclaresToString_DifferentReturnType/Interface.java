/* Hierarchy:
 * -----------------------------------------------------------------------------
 * JLS 9.2: If an interface has no direct superinterfaces, then the interface 
 * implicitly declares a public abstract member method m with signature s, 
 * return type r, and throws clause t corresponding to each public instance 
 * method m with signature s, return type r, and throws clause t declared in 
 * Object, unless a method with the same signature, same return type, 
 * and a compatible throws clause is explicitly declared by the interface.
 * 
 * It follows that it is a compile-time error if the interface declares a method
 * with the same signature and different return type or incompatible throws
 * clause.
 * -----------------------------------------------------------------------------
 * String toString() is public on Object, so the void return type is a
 * compile-time error
 */
public interface Interface {
    public void toString();
}
