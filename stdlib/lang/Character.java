package java.lang;
public final class Character {
    public char value;
    public Character(char i) {
        value = i;
    }
    public Character() {
    }
    public String toString() {
        return String.valueOf(value);
    }
}
