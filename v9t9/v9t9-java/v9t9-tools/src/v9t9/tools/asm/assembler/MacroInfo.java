/*
  MacroInfo.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm.assembler;

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