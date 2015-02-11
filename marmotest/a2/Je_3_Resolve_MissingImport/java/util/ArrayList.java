package java.util;
public class ArrayList implements List {
    protected int size = 0;
    protected Object[] os = new Object[1];
    public ArrayList() {
    }
    public int size() {
        return size;
    }
    public Object get(int index) {
        return os[index];
    }
    public Object set(int index, Object obj) {
        Object ret = os[index];
        os[index] = obj;
        return ret;
    }
    public boolean add(Object obj) {
        if(size >= os.length) {
            Object[] newos = new Object[os.length*2];
            for(int i = 0; i < size; i = i + 1) {
                newos[i] = os[i];
            }
            os = newos;
        }
        size = size + 1;
        if(os[size-1] == obj) return false;
        os[size-1] = obj;
        return true;
    }
    public Object remove(int index) {
        Object ret = os[index];
        size = size - 1;
        for(int i = index; i < size; i = i + 1) {
            os[i] = os[i+1];
        }
        return ret;
    }
}
