/*
  MachineOperandState.java

  (c) 2014-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.cpu;

import v9t9.common.asm.IMachineOperand;

public class MachineOperandState /*implements Cloneable*/ {
	public MachineOperandState(IMachineOperand mop) {
		this.mop = mop;
	}
	/** operand */
	public final IMachineOperand mop;
	/** cycles used by the operand fetch */
//	public int cycles;
	/** IChangeElement sets this */
	public short ea;
	/** IChangeElement sets this */
	public short value;
	/** Original value for revert */
	public short prev;
	
//	public MachineOperandState clone() {
//		try {
//			return (MachineOperandState) super.clone();
//		} catch (CloneNotSupportedException e) {
//			assert false;
//			return null;
//		}
//	}
}