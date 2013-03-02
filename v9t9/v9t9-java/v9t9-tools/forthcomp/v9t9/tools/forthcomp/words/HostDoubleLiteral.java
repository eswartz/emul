/*
  HostDoubleLiteral.java

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
public class HostDoubleLiteral extends BaseWord {

	private final int valLo;
	private final int valHi;
	private boolean isUnsigned;
	/**
	 * @param l 
	 * @param isUnsigned_ 
	 * 
	 */
	public HostDoubleLiteral(int valLo_, int valHi_, boolean isUnsigned_) {
		this.valLo = valLo_;
		this.valHi = valHi_;
		this.isUnsigned = isUnsigned_;
		
		setCompilationSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				hostContext.compile(HostDoubleLiteral.this);
				targetContext.compileDoubleLiteral(valLo, valHi, isUnsigned, true);
			}
		});
		setExecutionSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
				hostContext.pushData(valLo);
				hostContext.pushData(valHi);
			}
		});
	}
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#getValue()
	 */
	public int getValueLo() {
		return valLo;
	}
	/**
	 * @return the valHi
	 */
	public int getValueHi() {
		return valHi;
	}
	/**
	 * @param forField the forField to set
	 */
	public void setUnsigned(boolean forField) {
		this.isUnsigned = forField;
	}
	/**
	 * @return the forField
	 */
	public boolean isUnsigned() {
		return isUnsigned;
	}
	@Override
	public boolean isCompilerWord() {
		return true;
	}
}
