/**
 * Mar 15, 2011
 */
package v9t9.tools.forthcomp;

import v9t9.engine.memory.MemoryDomain;
import v9t9.tools.forthcomp.words.TargetContext;

/**
 * @author ejs
 *
 */
public class GromDictEntry extends DictEntry {

	private int dictAddr;

	/**
	 * @param headerSize
	 * @param addr
	 * @param name
	 */
	public GromDictEntry(int headerSize, int addr, String name, int gp) {
		super(headerSize, addr, name);
		this.dictAddr = gp;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.DictEntry#getContentAddr()
	 */
	@Override
	public int getContentAddr() {
		return addr;
	}
	/**
	 * @return the dictAddr
	 */
	public int getDictAddr() {
		return dictAddr;
	}
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.DictEntry#writeEntry(v9t9.forthcomp.words.TargetContext)
	 */
	@Override
	public void writeEntry(TargetContext targetContext) {
		byte[] ent = doWriteEntry(targetContext);
		
		// ignore link
		MemoryDomain domain = ((F99bTargetContext) targetContext).getGrom();
		int gp = dictAddr;
		for (int i = 0; i < ent.length - targetContext.getCellSize(); i++) {
			domain.writeByte(gp++, ent[i + targetContext.getCellSize()]);
		}
		
		// place xt
		targetContext.writeCell(ent, 0, getContentAddr());
		for (int i = 0; i < targetContext.getCellSize(); i++) {
			domain.writeByte(gp++, ent[i]);
		}

	}
}
