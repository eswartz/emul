/**
 * 
 */
package v9t9.tools.asm;

import java.util.Map;

public class MacroInfo {
	String[] argNames;
	private final String template;
	
	public MacroInfo(String[] argNames, String template) {
		this.argNames = argNames;
		this.template = template;
	}
	
	public String expand(Map<String, String> vars) {
		String expansion = template;
		for (Map.Entry<String, String> entry : vars.entrySet()) {
			String var = "\\$\\{" + entry.getKey() + "\\}";
			expansion = expansion.replaceAll(var, entry.getValue());
		}
		return expansion;
	}
}