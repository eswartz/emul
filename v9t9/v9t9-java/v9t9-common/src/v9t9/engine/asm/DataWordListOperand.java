/**
 * 
 */
package v9t9.engine.asm;

import org.ejs.coffee.core.utils.HexUtils;

import v9t9.engine.cpu.Operand;

/**
 * @author ejs
 *
 */
public class DataWordListOperand implements Operand {

	private int[] args;

	public DataWordListOperand(int[] args) {
		this.args = args;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("args = { ");
		for (int i = 0; i < args.length; i++) {
			if (i > 0)
				builder.append(", ");
			builder.append('>');
			builder.append(HexUtils.toHex4(args[i]));
		}
		builder.append(" }");
		return builder.toString();
	}
}
