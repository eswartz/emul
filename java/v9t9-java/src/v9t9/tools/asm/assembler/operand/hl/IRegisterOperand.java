/**
 * 
 */
package v9t9.tools.asm.assembler.operand.hl;

/**
 * @author ejs
 *
 */
public interface IRegisterOperand {

	AssemblerOperand getReg();

	boolean isReg(int reg);

}