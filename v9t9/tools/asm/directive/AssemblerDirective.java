/**
 * 
 */
package v9t9.tools.asm.directive;

import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.Instruction;

/**
 * @author Ed
 *
 */
public abstract class AssemblerDirective implements IInstruction {
	protected static Instruction[] NO_INSTRUCTIONS = new Instruction[0];
	private int pc;
	
	public AssemblerDirective() {
		this.pc = 0;
	}
	
	public int getPc() {
		return pc;
	}
	
	public void setPc(int pc) {
		this.pc = pc;
	}
	
	private static final byte[] NO_BYTES = new byte[0];
	
	public byte[] getBytes() {
		return NO_BYTES;
	}

}
