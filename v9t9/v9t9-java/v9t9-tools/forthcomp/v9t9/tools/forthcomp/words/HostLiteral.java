/*
  HostLiteral.java

  (c) 2010-2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.ISemantics;

/**
 * @author ejs
 *
 */
public class HostLiteral extends BaseWord {

	private final int val;
	private boolean isUnsigned; 
	/**
	 * @param isUnsigned 
	 * 
	 */
	public HostLiteral(int val_, boolean isUnsigned_) {
		this.val = val_;
		this.isUnsigned = isUnsigned_;
		setCompilationSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				hostContext.compile(HostLiteral.this);
				targetContext.compileLiteral(val, isUnsigned, true);
			}
		});
		setExecutionSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
				hostContext.pushData(val);
			}
		});
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "LITERAL "  + val;
	}
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#getValue()
	 */
	public int getValue() {
		return val;
	}
	/**
	 * @param forField the forField to set
	 */
	public void setUnsigned(boolean isUnsigned) {
		this.isUnsigned = isUnsigned;
	}
	public boolean isUnsigned() {
		return isUnsigned;
	}

	@Override
	public boolean isCompilerWord() {
		return true;
	}
}
