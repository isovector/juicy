// TYPE_LINKING
/* TypeLinking:
 * Check that no prefixes (consisting of whole identifiers) of fully qualified 
 * types themselves resolve to types. 
 */
public class Main {
  public Main() {}

  public static int test() {
    foo.String.bar.foo s = new foo.String.bar.foo();
    return 123;
  }
} 
