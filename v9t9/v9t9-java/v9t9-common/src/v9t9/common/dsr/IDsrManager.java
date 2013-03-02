/*
  IDsrManager.java

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
package v9t9.common.dsr;

import java.util.List;

import ejs.base.settings.ISettingSection;


import v9t9.common.cpu.InstructionWorkBlock;

public interface IDsrManager {

	void dispose();

	void saveState(ISettingSection section);

	void loadState(ISettingSection section);

	/**
	 * @return the dsrs
	 */
	List<IDsrHandler> getDsrs();

	void registerDsr(IDsrHandler dsr);

	/**
	 *	Your DSR module may use the DSR opcode range (OP_DSR) to make
	 *	callbacks into V9t9 to handle subroutine calls.  This routine
	 *	handles those opcodes by calling the DSR module 'filehandler'
	 *	callback.
	 */
	void handleDSR(InstructionWorkBlock instructionWorkBlock);

}