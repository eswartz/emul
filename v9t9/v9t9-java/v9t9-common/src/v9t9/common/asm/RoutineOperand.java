/*
  RoutineOperand.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.asm;


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
