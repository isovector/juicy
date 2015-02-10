package java.lang;
public class Object {
    public Object() {
    }
    public boolean equals(Object other) {
        return this == other;
    }
    public String toString() {
        return "Some random object";
    }
    public int hashCode() {
        return 42;
    }
    protected Object clone() {
        return this;
    }
    public final Class getClass() {
        return null;
    }
}
