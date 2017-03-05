/*
  TargetDoesWord.java

  (c) 2010-2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.ISemantics;
import v9t9.tools.forthcomp.ITargetWord;
import v9t9.tools.forthcomp.TargetContext;

/**
 * @author ejs
 *
 */
public class TargetDoesWord extends TargetWord {

	private final ITargetWord var;
	private final int doesPc;

	/**
	 * @param lastEntry
	 */
	public TargetDoesWord(ITargetWord var_, int doesPc_) {
		super(var_.getEntry());
		this.var = var_;
		this.doesPc = doesPc_;
		
		setExecutionSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				if (HostContext.DEBUG)
					System.out.println("T> does> " + Integer.toHexString(var.getEntry().getParamAddr()) + "(" + doesPc + ")");
				
				hostContext.pushData(var.getEntry().getParamAddr());
				hostContext.pushCall(doesPc);
				hostContext.interpret(hostContext, targetContext);				
			}
		});
	}
	
}
