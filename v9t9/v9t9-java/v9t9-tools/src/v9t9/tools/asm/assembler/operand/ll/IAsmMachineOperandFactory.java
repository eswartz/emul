/*
  IAsmMachineOperandFactory.java

  (c) 2010-2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.tools.asm.assembler.operand.ll;

import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.ResolveException;

public interface IAsmMachineOperandFactory {
	//MachineOperand createMachineOperand(LLOperand op) throws ResolveException;
	IMachineOperand createRegisterOperand(LLRegisterOperand op) throws ResolveException;
	IMachineOperand createAddressOperand(LLAddrOperand op) throws ResolveException;
	IMachineOperand createCountOperand(LLCountOperand op) throws ResolveException;
	IMachineOperand createImmedOperand(LLImmedOperand op) throws ResolveException;
	IMachineOperand createPCRelativeOperand(LLPCRelativeOperand op) throws ResolveException;
	IMachineOperand createOffsetOperand(LLOffsetOperand op) throws ResolveException;
	IMachineOperand createRegIncOperand(LLRegIncOperand op) throws ResolveException;
	IMachineOperand createRegIndOperand(LLRegIndOperand op) throws ResolveException;
	IMachineOperand createRegOffsOperand(LLRegOffsOperand op) throws ResolveException;
	IMachineOperand createEmptyOperand();
	IMachineOperand createRegDecOperand(LLRegDecOperand op) throws ResolveException;
	IMachineOperand createScaledRegOffsOperand(LLScaledRegOffsOperand op) throws ResolveException;
		
}
