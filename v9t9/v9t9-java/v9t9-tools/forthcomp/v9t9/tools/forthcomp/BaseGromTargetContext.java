/**
 * 
 */
package v9t9.tools.forthcomp;

import v9t9.engine.memory.MemoryDomain;

/**
 * @author ejs
 *
 */
public abstract class BaseGromTargetContext extends TargetContext implements IGromTargetContext {

	protected boolean useGromDictionary;
	protected int gp;
	private MemoryDomain grom;

	public BaseGromTargetContext(boolean littleEndian, int charBits,
			int cellBits, int memorySize) {
		super(littleEndian, charBits, cellBits, memorySize);
	}

	@Override
	public boolean useGromDictionary() {
		return useGromDictionary;
	}

	@Override
	public void setUseGromDictionary(boolean useGromDictionary) {
		this.useGromDictionary = useGromDictionary;
	}

	@Override
	public int getGP() {
		return gp;
	}

	/**
	 * @param gp the gp to set
	 */
	@Override
	public void setGP(int gp) {
		this.gp = gp;
	}

	/**
	 * @param grom the grom to set
	 */
	public void setGrom(MemoryDomain grom) {
		this.grom = grom;
	}

	/**
	 * @return
	 */
	public MemoryDomain getGrom() {
		return grom;
	}


	/* (non-Javadoc)
	 * @see v9t9.forthcomp.words.TargetContext#createDictEntry(int, int, java.lang.String)
	 */
	@Override
	protected DictEntry createDictEntry(int size, int entryAddr, String name, boolean doExport) {
		
		if (useGromDictionary && doExport) {

			// link (=>xt), name
			int dictSize = cellSize + 1 + name.length();
			DictEntry entry = new GromDictEntry(dictSize, entryAddr, name, gp);
			gp += dictSize;
			return entry;
		} else {
			return super.createDictEntry(size, entryAddr, name, doExport);
		}
	}

}