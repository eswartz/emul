/*
  TargetUserVariable.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.DictEntry;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.ISemantics;
import v9t9.tools.forthcomp.TargetContext;

/**
 * @author ejs
 *
 */
public class TargetUserVariable extends TargetWord {

	private int offset;
	/**
	 * @param offset 
	 * 
	 */
	public TargetUserVariable(DictEntry entry, int offset) {
		super(entry);
		this.offset = offset;
		
		setCompilationSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				if (targetContext.isNativeDefinition()) {
					targetContext.compileUser(TargetUserVariable.this);
				} else {
					targetContext.buildCall(TargetUserVariable.this);
				}
			}
		});
		setExecutionSemantics(new ISemantics() {
			
			@Override
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				hostContext.pushData(0xff00 + (TargetUserVariable.this.getOffset()));				
			}
		});
	}

	public int getOffset() {
		return offset;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.TargetWord#toString()
	 */
	@Override
	public String toString() {
		return "User " + super.toString();
	}
}
