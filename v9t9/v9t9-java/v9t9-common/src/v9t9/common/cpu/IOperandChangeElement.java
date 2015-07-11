/**
 * 
 */
package v9t9.common.cpu;

import v9t9.common.asm.IOperand;

/**
 * @author ejs
 *
 */
public interface IOperandChangeElement extends IChangeElement {

	String format(IOperand op, boolean preExecute);
}
