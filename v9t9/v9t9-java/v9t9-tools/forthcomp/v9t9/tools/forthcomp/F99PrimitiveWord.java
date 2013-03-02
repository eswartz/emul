/*
  F99PrimitiveWord.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp;

import v9t9.tools.forthcomp.words.TargetContext;
import v9t9.tools.forthcomp.words.TargetWord;

/**
 * @author ejs
 *
 */
public class F99PrimitiveWord extends TargetWord {

	private final int opcode;

	/**
	 * @param entry
	 */
	public F99PrimitiveWord(final DictEntry entry, int opcode) {
		super(entry);
		this.opcode = opcode;
		
		setCompilationSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				int opcode = getOpcode();
				targetContext.compileOpcode(opcode);
				entry.use();
			}
		});
	}

	/**
	 * @return the opcode
	 */
	public int getOpcode() {
		return opcode;
	}

}
