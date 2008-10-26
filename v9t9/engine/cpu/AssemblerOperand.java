/**
 * 
 */
package v9t9.engine.cpu;

import v9t9.tools.asm.Assembler;
import v9t9.tools.asm.ResolveException;

/**
 * @author ejs
 *
 */
public interface AssemblerOperand extends Operand {
    /** Resolve yourself to a machine operand. 
     * @throws ResolveException if the operand cannot be resolved.
     * */
	MachineOperand resolve(Assembler assembler, Instruction inst) throws ResolveException;

}
