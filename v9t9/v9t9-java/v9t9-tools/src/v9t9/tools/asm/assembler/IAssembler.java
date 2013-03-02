/*
  IAssembler.java

  (c) 2011 Edward Swartz

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
package v9t9.tools.asm.assembler;

import java.util.List;
import java.util.Stack;

import v9t9.common.asm.IInstruction;
import v9t9.tools.asm.assembler.transform.ConstPool;

/**
 * @author ejs
 *
 */
public interface IAssembler {
	public void setProcessor(String proc);
	
	SymbolTable getSymbolTable();
	IAsmInstructionFactory getInstructionFactory() ;

	public int getPc();
	public void setPc(int immed) ;
	
	public ConstPool getConstPool();

	public Symbol referenceSymbol(String string);

	/**
	 * @return
	 */
	public String getNextLine();

	/**
	 * @param macroContentEntry
	 */
	public void pushContentEntry(ContentEntry macroContentEntry);

	/**
	 * @param labelName
	 * @return
	 */
	public Symbol findForwardLocalLabel(String labelName);

	/**
	 * @param labelName
	 * @return
	 * @throws ParseException 
	 */
	public Symbol findBackwardLocalLabel(String labelName) throws ParseException;

	/**
	 * @return
	 */
	public int getBasicAlignment();

	/**
	 * @return
	 */
	public Stack<ContentEntry> getContentEntryStack();

	/**
	 * 
	 */
	public void pushSymbolTable();

	/**
	 * @throws ParseException 
	 * 
	 */
	public void popSymbolTable() throws ParseException;

	/**
	 * @param singletonList
	 * @return
	 */
	public List<IInstruction> resolve(List<IInstruction> singletonList);
}