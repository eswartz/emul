/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 22, 2006
 *
 */
package v9t9.tools.decomp;

import v9t9.engine.cpu.Operand;
import v9t9.utils.Check;

public class LabelOperand implements Operand {

    public Label label;

    public LabelOperand(Label label) {
    	Check.checkArg(label);
        this.label = label;
    }
    
    @Override
    public String toString() {
        return label.toString();
    }

}
