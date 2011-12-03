/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 26, 2006
 *
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
