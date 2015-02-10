package java.lang;
public final class Byte {
    public byte value;
    public Byte(byte i) {
        value = i;
    }
    public Byte() {
    }
    public String toString() {
        return String.valueOf(value);
    }
    public static byte MAX_VALUE = (byte)127;
}
