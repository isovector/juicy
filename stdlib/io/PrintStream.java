package java.io;
public class PrintStream extends OutputStream {
    public PrintStream() {
    }
    public void print(String s) {
        for(int i = 0; i < s.length(); i = i + 1) {
            write(s.charAt(i));
        }
    }
    public void println() {
        println("");
    }
    public void println(String s) {
        print(s);
        write('\n');
    }
    public void println(Object b) {
        println(String.valueOf(b));
    }
    public void println(boolean b) {
        println(String.valueOf(b));
    }
    public void println(byte b) {
        println(String.valueOf(b));
    }
    public void println(char b) {
        println(String.valueOf(b));
    }
    public void println(short b) {
        println(String.valueOf(b));
    }
    public void println(int b) {
        println(String.valueOf(b));
    }
    public void print(Object b) {
        print(String.valueOf(b));
    }
    public void print(boolean b) {
        print(String.valueOf(b));
    }
    public void print(byte b) {
        print(String.valueOf(b));
    }
    public void print(char b) {
        print(String.valueOf(b));
    }
    public void print(short b) {
        print(String.valueOf(b));
    }
    public void print(int b) {
        print(String.valueOf(b));
    }
}
