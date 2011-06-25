/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 25, 2006
 *
 */
package v9t9.tools.asm.decomp;

import v9t9.tools.asm.common.LabelOperand;

public class RoutineOperand extends LabelOperand {

    public Routine routine;

    public RoutineOperand(Routine routine) {
        super(routine.getMainLabel());
        this.routine = routine;
    }

    @Override
    public String toString() {
        return "("+routine.getMainLabel()+")";
    }
}
