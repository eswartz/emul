/**
 * 
 */
package v9t9.tools.asm.assembler;

import java.util.List;
import java.util.Stack;

import v9t9.engine.cpu.IInstruction;
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