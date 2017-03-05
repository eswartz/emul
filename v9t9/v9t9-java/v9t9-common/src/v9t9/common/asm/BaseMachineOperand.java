/*
  BaseMachineOperand.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.asm;


/**
 * @author Ed
 *
 */
public abstract class BaseMachineOperand implements IMachineOperand {

	public int type = OP_NONE;
	/** value in opcode, usually register or count */
	public int val = 0;
	/** immediate word */
	public short immed = 0;
	public int dest = OP_DEST_FALSE;
	public boolean bIsReference = false;
}