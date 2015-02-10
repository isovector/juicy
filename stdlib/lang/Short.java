package java.lang;
public final class Short extends Number {
    public short value;
    public Short(short i) {
        value = i;
    }
    public int intValue() {
        return (int) value;
    }
    public Short() {
    }
    public String toString() {
        return String.valueOf(value);
    }
}
