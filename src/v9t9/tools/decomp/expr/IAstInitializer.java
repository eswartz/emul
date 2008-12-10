/**
 * 
 */
package v9t9.tools.decomp.expr;

/**
 * Base interface for an initializer.
 * 
 * This contains a source range that includes the "=" sign.
 * (though it needn't necessary <i>start</i> at '=').
 * 
 * @author eswartz
 *
 */
public interface IAstInitializer extends IAstNode {
    public static final IAstInitializer EMPTY_ARRAY[] = new IAstInitializer[0];
}
