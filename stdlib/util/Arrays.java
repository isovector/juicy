package java.util;

public class Arrays {
    public Arrays() {
    }
    public static boolean equals(boolean[] a1, boolean[] a2) {
        if(a1.length != a2.length) return false;
        for(int i = 0; i < a1.length; i = i + 1)
            if(a1[i] != a2[i])
                return false;
        return true;
    }
    public static boolean equals(char[] a1, char[] a2) {
        if(a1.length != a2.length) return false;
        for(int i = 0; i < a1.length; i = i + 1)
            if(a1[i] != a2[i])
                return false;
        return true;
    }
}
