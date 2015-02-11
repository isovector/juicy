/* TypeLinking:
 * Check that all import-on-demand declarations refer to existing packages.
 * 
 * Main is not a package
 */

import Main.*;

public class A {
    public A() {}
}
