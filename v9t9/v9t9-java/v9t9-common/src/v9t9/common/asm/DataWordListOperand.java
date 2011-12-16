/**
 * 
 */
package v9t9.common.asm;


import ejs.base.utils.HexUtils;

/**
 * @author ejs
 *
 */
public class DataWordListOperand implements IOperand {

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
