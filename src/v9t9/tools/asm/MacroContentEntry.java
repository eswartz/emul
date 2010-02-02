/**
 * 
 */
package v9t9.tools.asm;

import java.util.List;

/**
 * @author Ed
 *
 */
public class MacroContentEntry extends ContentEntry {

	private final List<String> varargs;

	public MacroContentEntry(String name, String text, List<String> varargs) {
		super(name, text);
		this.varargs = varargs;
	}

	public List<String> getVarargs() {
		return varargs;
	}
	
}
