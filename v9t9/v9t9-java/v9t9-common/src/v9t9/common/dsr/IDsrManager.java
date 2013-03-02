/*
  IDsrManager.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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