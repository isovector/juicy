package java.util;
public class LinkedList implements List {
    protected boolean empty = true;
    protected Object cur = null;
    protected LinkedList next = null;
    public LinkedList() {
    }
    public int size() {
        if(empty) return 0;
        else if(next == null) return 1;
        else return 1+next.size();
    }
    public boolean add(Object o) {
        if(empty) {
            cur = o;
            empty = false;
        } else {
            if(next == null) {
                next = new LinkedList();
            }
            next.add(o);
        }
        return true;
    }
    public Object clone() {
        LinkedList ret = new LinkedList();
        if(next != null) ret.next = (LinkedList) next.clone();
        ret.cur = cur;
        ret.empty = empty;
        return ret;
    }
}
