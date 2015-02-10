package java.lang;
public final class Boolean {
    public boolean value;
    public Boolean(boolean i) {
        value = i;
    }
    public Boolean() {
    }
    public String toString() {
        return String.valueOf(value);
    }
    public static byte MAX_VALUE = (byte)127;
}
