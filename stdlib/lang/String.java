package java.lang;
import java.util.Arrays;

public class String {
    public char[] chars;
    public int length() {
        return chars.length;
    }
    public char charAt(int i) {
        return chars[i];
    }
    public String() {
        chars = new char[0];
    }
    public String(char[] chars) {
        this.chars = new char[chars.length];
        for(int i = 0; i < chars.length; i = i + 1) this.chars[i] = chars[i];
    }
    public String(String other) {
        this.chars = other.chars;
    }
    public String concat(String s2) {
        int i = 0;
        char[] newchars = new char[length() + s2.length()];
        for(i = 0; i < length(); i = i + 1)
            newchars[i] = chars[i];
        for(i = 0; i < s2.length(); i = i + 1)
            newchars[i+length()] = s2.chars[i];
        return new String(newchars);
    }
    public static String valueOf(char c) {
        char[] newchars = new char[1];
        newchars[0] = c;
        return new String(newchars);
    }
    public static String valueOf(int i) {
        char[] ret = new char[15];
        int j = 0;
        boolean wasneg = false;
        if(i == -2147483648) return "-2147483648";
        if(i<0) {
            wasneg = true;
            i = -i;
        }
        if(i == 0) {
            ret[j] = '0';
            j = j + 1;
        } else {
            while(i > 0) {
                int d = i % 10;
                i = i / 10;
                ret[j] = (char) (d + '0');
                j = j + 1;
            }
        }
        if(wasneg) {
            ret[j] = '-';
            j = j + 1;
        }
        char[] ret2 = new char[j];
        for(i = 0; i < j; i = i + 1) ret2[i] = ret[j-1-i];
        return new String(ret2);
    }
    public static String valueOf(short i) {
        return String.valueOf((int) i);
    }
    public static String valueOf(byte i) {
        return String.valueOf((int) i);
    }
    public static String valueOf(boolean b) {
        if(b) return "true"; else return "false";
    }
    public static String valueOf(Object o) {
        if(o == null)  return "null"; else return o.toString();
    }
    public static String valueOf(String o) {
        if(o == null)  return "null"; else return o;
    }
    public boolean equals(Object o) {
        if(o == null) return false;
        if(!(o instanceof String)) return false;
        return Arrays.equals(chars, ((String)o).chars);
    }
    public String substring(int i, int j) {
        int k = 0;
        if(i<0) return "";
        if(j>length()) return "";
        if(j<i) return "";
        char[] ret = new char[j-i];
        for(k=i;k<j;k=k+1) ret[k-i] = charAt(k);
        return new String(ret);
    }
    public String trim() {
        int i = 0;
        int j = 0;
        for(i=0;i<length() && charAt(i)<=' ';i=i+1) {}
        for(j=length()-1;j>=0 && charAt(j)<=' ';j=j-1) {}
        if(i>j) return ""; else return substring(i,j+1);
    }
    public int hashCode() {
        int h = 0;
        for (int i = 0; i < chars.length; i = i+1) {
            h = 31*h + chars[i];
        }

        return h;
    }
    public String toString() {
        return this;
    }
    public int compareTo(Object other) {
        return compareTo((String) other);
    }
    public int compareTo(String other) {
        int i = 0;
        boolean b = true;
        while(b) {
            if(i >= chars.length && i >= other.chars.length) return 0;
            if(i >= chars.length) return -1;
            if(i >= other.chars.length) return 1;
            if(chars[i] < other.chars[i])  return -1;
            if(chars[i] > other.chars[i])  return 1;
        }
        return 0;
    }
    public char[] toCharArray() {
        char[] ret = new char[chars.length];
        for(int i = 0; i < ret.length; i = i+1) {
            ret[i] = chars[i];
        }
        return ret;
    }
    public int indexOf(String needle) {
        int offset = 0;
        int i = 0;
        for(offset = 0; offset < length(); offset = offset + 1) {
            boolean found = true;
            for(i = 0; i < needle.length(); i = i + 1) {
                if(i+offset >= length()) found = false;
                else if(chars[i+offset] != needle.chars[i]) found = false;
            }
            if(found) return offset;
        }
        return -1;
    }
}
