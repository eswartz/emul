/*
  TargetConstant.java

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

/**
 * @author ejs
 *
 */
public class TargetConstant extends TargetWord implements ITargetWord {

	private final int value;
	private final int width;

	/**
	 * @param entry
	 */
	public TargetConstant(DictEntry entry, int value_, int width_) {
		super(entry);
		this.value = value_;
		this.width = width_;
		
		setCompilationSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				if (getWidth() == 1)
					targetContext.buildLiteral(getValue(), false, true);
				else if (getWidth() == 2 && targetContext.getCellSize() == 2)
					targetContext.buildDoubleLiteral(getValue() & 0xffff, getValue() >> 16, false, true);
				else
					assert false;
			}
		});
		setExecutionSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				hostContext.pushData(value & 0xffff);
				if (width == 2)
					hostContext.pushData(value >> 16);
				
			}
		});
	}

	/**
	 * @return
	 */
	public int getValue() {
		return value;
	}
	
	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}
}
