/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 22, 2006
 *
 */
package v9t9.common.asm;


import ejs.base.utils.Check;

public class LabelOperand implements IOperand {

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
