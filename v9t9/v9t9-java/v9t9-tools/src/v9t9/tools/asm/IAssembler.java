/*
  IAssembler.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm;

import java.util.List;
import java.util.Stack;

import v9t9.common.asm.IInstruction;
import v9t9.tools.asm.transform.ConstPool;

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