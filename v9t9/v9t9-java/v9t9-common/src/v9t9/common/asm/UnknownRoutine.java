/*
  UnknownRoutine.java

  (c) 2008-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.asm;



/**
 * @author ejs
 *
 */
public class UnknownRoutine extends Routine {

	/* (non-Javadoc)
	 * @see v9t9.tools.decomp.Routine#examineEntryCode()
	 */
	@Override
	public void examineEntryCode() {

	}

	/* (non-Javadoc)
	 * @see v9t9.tools.decomp.Routine#isReturn(v9t9.tools.decomp.LLInstruction)
	 */
	@Override
	public boolean isReturn(IHighLevelInstruction inst) {
		return false;
	}

}
