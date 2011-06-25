/**
 * 
 */
package v9t9.tools.asm.assembler.operand.hl;

/**
 * @author ejs
 *
 */
public interface IOperandVisitor {
	class Terminate extends RuntimeException { private static final long serialVersionUID = 1L; }
	boolean enterOperand(AssemblerOperand operand) throws Terminate;
	void exitOperand(AssemblerOperand operand) throws Terminate;
}
