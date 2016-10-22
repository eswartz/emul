/*
  Disassembler.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm.decomp;

import java.io.PrintStream;
import java.util.BitSet;

import ejs.base.utils.HexUtils;

import v9t9.common.asm.RawInstruction;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryDomain;
import v9t9.tools.utils.ToolUtils;

/**
 * @author ejs
 *
 */
public class Disassembler {

	private IMachine machine;
	private boolean plain;

	/**
	 * @param machine
	 */
	public Disassembler(IMachine machine) {
		this.machine = machine;
	}

	/**
	 * @param b
	 */
	public void setShowPlain(boolean plain) {
		this.plain = plain;
		
	}

	/**
	 * @param codeAddrs 
	 * @param os
	 */
	public void dumpCode(BitSet codeAddrs, PrintStream os) {
		int size = 1;
		IMemoryDomain console = machine.getConsole();
		StringBuilder sb = new StringBuilder();
		
		int expNext = -1;
		
		for (int addr = codeAddrs.nextSetBit(0); addr >= 0; addr = codeAddrs.nextSetBit(addr + size)) {
			RawInstruction instr = machine.getInstructionFactory().decodeInstruction(addr, console);
			if (!plain) {
				sb.setLength(0);
				sb.append(HexUtils.toHex4(addr)).append("  ");
				ToolUtils.appendInstructionCode(machine, 
						sb, instr);
				os.print(sb);
			} else {
				if (addr != expNext)
					os.println("  AORG >" + HexUtils.toHex4(addr));
				os.print("  ");
			}
			os.println(instr.toString());
			size = instr.getSize();
			expNext = addr + size;
		}
	}

}
