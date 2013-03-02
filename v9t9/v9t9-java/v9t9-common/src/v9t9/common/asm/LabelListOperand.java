/*
  LabelListOperand.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.asm;

import java.util.Iterator;
import java.util.List;


public class LabelListOperand implements IOperand {
    public List<LabelOperand> operands;
	private IMachineOperand mop;

    public LabelListOperand(IMachineOperand mop, List<LabelOperand> operands) {
    	this.mop = mop;
        this.operands = operands;
    }
    
    public Iterator<LabelOperand> iterator() {
        return operands.iterator();
    }
    
    @Override
    public String toString() {
        return mop + " : " + operands.toString();
    }
    
    public IMachineOperand getMachineOperand() {
    	return mop;
    }
}
