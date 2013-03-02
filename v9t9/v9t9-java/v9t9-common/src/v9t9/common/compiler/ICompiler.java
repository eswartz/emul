/*
  ICompiler.java

  (c) 2011-2013 Edward Swartz

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
package v9t9.common.compiler;

import v9t9.common.asm.IDecompileInfo;
import v9t9.common.asm.RawInstruction;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.settings.SettingSchema;

/**
 * @author ejs
 *
 */
public interface ICompiler {

	SettingSchema settingOptimize = new SettingSchema(
			ISettingsHandler.MACHINE,
			"CompilerOptimize", Boolean.FALSE);
	SettingSchema settingOptimizeRegAccess = new SettingSchema(
			ISettingsHandler.MACHINE,
			"CompilerOptimizeRegAccess", Boolean.TRUE);
	SettingSchema settingOptimizeStatus = new SettingSchema(
			ISettingsHandler.MACHINE,
			"CompilerOptimizeStatus", Boolean.FALSE);
	SettingSchema settingCompileOptimizeCallsWithData = new SettingSchema(
			ISettingsHandler.MACHINE,
			"CompilerOptmizeCallsWithData", Boolean.FALSE);
	SettingSchema settingDebugInstructions = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"DebugInstructions", Boolean.TRUE);
	SettingSchema settingCompileFunctions = new SettingSchema(
			ISettingsHandler.MACHINE,
			"CompilerCompileFunctions", Boolean.FALSE);

	/**
	 * Compile the instructions into bytecode.
	 * @param uniqueClassName
	 * @param baseName
	 * @param highLevel
	 * @param insts
	 * @param entries
	 * @return
	 */
	byte[] compile(String uniqueClassName, String baseName,
			IDecompileInfo highLevel, RawInstruction[] insts, short[] entries);

	/**
	 * Tell if the CPU is coherent and compilation makes sense
	 * @return
	 */
	boolean validCpuState();

	/** Currently, only gather high-level info for one memory entry at a time */
	IDecompileInfo getHighLevelCode(IMemoryEntry entry);

}