/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 22, 2006
 *
 */
package v9t9.engine.asm;

import org.ejs.coffee.core.utils.Check;

import v9t9.engine.cpu.Operand;

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
