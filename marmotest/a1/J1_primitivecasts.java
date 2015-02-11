// PARSER_WEEDER,TYPE_CHECKING
public class J1_primitivecasts {
  public J1_primitivecasts() {}
  public static int test() {
      boolean t = (boolean)true;
      char c = (char)'y';
      short s = (short)17;
      byte b = (byte)5;
      return (int)123;
  }
}
