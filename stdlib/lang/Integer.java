package java.lang;
public final class Integer extends Number {
    public int value;
    public Integer(int i) {
        value = i;
    }
    public int intValue() {
        return value;
    }
    public static int parseInt(String s) {
        int ret = 0;
        boolean neg = false;
        int i = 0;
        while(i < s.length() && 
          (s.charAt(i) == '-' || (s.charAt(i) >= '0' && s.charAt(i) <= '9'))) {
            if(s.charAt(i) == '-') neg = !neg;
            else {
                ret = ret * 10 + s.charAt(i)-'0';
            }
            i = i+1;
        }
        if(neg) ret = -ret;
        return ret;
    }
    public Integer(String s) {
        value = Integer.parseInt(s);
    }
    public Integer() {
        value = 0;
    }
    public String toString() {
        return String.valueOf(value);
    }
    public static int MAX_VALUE = 2147483647;
}
