/**
 * 
 */
package v9t9.engine.cpu;


/**
 * @author Ed
 *
 */
public interface IInstruction {

	int getPc();

	String toInfoString();

	boolean isByteOp();
}
