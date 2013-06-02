/*
  Decompiler9900.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm.decomp;

import v9t9.common.machine.IMachine;
import v9t9.tools.asm.inst9900.AsmInstructionFactory9900;

public class Decompiler9900 extends Decompiler {
    public Decompiler9900(IMachine machine) {
		super(machine, AsmInstructionFactory9900.INSTANCE);
    }
}
