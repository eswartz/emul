/**
 * 
 */
package v9t9.tools.asm.directive;

import v9t9.engine.cpu.IInstruction;
import v9t9.tools.asm.Assembler;
import v9t9.tools.asm.ResolveException;

/**
 * @author Ed
 *
 */
public class DescrDirective extends AssemblerDirective {

	private final String content;
	private final int line;
	private final String filename;

	public DescrDirective(String filename, int line, String content) {
		this.filename = filename;
		this.line = line;
		this.content = content;
	}

	@Override
	public String toString() {
		return filename + ":" + line;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.IInstruction#resolve(v9t9.tools.asm.Assembler, v9t9.engine.cpu.IInstruction)
	 */
	public IInstruction[] resolve(Assembler assembler, IInstruction previous, boolean finalPass)
			throws ResolveException {
		return new IInstruction[] { this };
	}

	public String getFilename() {
		return filename;
	}
	public int getLine() {
		return line;
	}
	public String getContent() {
		return content;
	}

}
