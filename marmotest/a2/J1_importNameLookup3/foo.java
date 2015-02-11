import bar.*;

public class foo {
    
    public int x;

    public foo() {
	x = 123;	
    }

    public int getX() {
	foo f = new foo();
	return f.x;
    }
}

