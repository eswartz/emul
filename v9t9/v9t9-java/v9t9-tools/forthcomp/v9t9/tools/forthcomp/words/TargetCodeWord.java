/*
  TargetColonWord.java

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
import v9t9.tools.forthcomp.ITargetWord;
import v9t9.tools.forthcomp.TargetContext;

/**
 * @author ejs
 *
 */
public class TargetCodeWord extends TargetWord implements ITargetWord, IPrimitiveWord {

	/**
	 * @param entry
	 */
	public TargetCodeWord(DictEntry entry) {
		super(entry);
		
		entry.setTargetOnly(true);
		
//		setCompilationSemantics(new ISemantics() {
//			
//			public void execute(HostContext hostContext, TargetContext targetContext)
//					throws AbortException {
//				targetContext.buildCall(TargetCodeWord.this);
//			}
//		});
		setExecutionSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				throw hostContext.abort("cannot execute target word: " + getEntry().getName());
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.TargetWord#toString()
	 */
	@Override
	public String toString() {
		return "Code " + getEntry().getName();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.IPrimitiveWord#getPrimitiveSize()
	 */
	@Override
	public int getPrimitiveSize() {
		return getEntry().getCodeSize();
	}

}
