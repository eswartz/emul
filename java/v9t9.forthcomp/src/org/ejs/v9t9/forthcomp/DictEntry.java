/**
 * 
 */
package org.ejs.v9t9.forthcomp;

/**
 * @author ejs
 *
 */
public class DictEntry {


	private int link;
	private int addr;
	private String name;
	private int headerSize;
	private int codeSize;
	private boolean hidden;
	private boolean immediate;

	/**
	 * @param name 
	 * 
	 */
	public DictEntry(int headerSize, int addr, String name) {
		this.headerSize = headerSize;
		this.addr = addr;
		this.name = name;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.ITargetWord#getHeaderSize()
	 */
	public int getHeaderSize() {
		return headerSize;
	}
	
	public int getContentAddr() {
		return addr + headerSize;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.ITargetWord#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.ITargetWord#getAddr()
	 */
	public int getAddr() {
		return addr;
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.ITargetWord#getLink()
	 */
	public int getLink() {
		return link;
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.ITargetWord#setLink(int)
	 */
	public void setLink(int link) {
		this.link = link;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	/**
	 * @return the hidden
	 */
	public boolean isHidden() {
		return hidden;
	}
	/**
	 * @param targetContext
	 */
	public void writeEntry(TargetContext targetContext) {
		int ad = addr;
		targetContext.writeCell(ad, link);  ad += targetContext.getCellSize();
		
		int flags = (hidden ? 0 : 0x80) | (immediate ? 0x40 : 0);
		
		targetContext.writeChar(ad, (name.length() & 0x1f) | flags); ad += 1;
		for (int i = 0; i < name.length(); i++)
			targetContext.writeChar(ad + i, name.charAt(i));
		ad += name.length();
		
		while (ad < addr + headerSize)
			targetContext.writeChar(ad++, 32);
			
	}

	/**
	 * @return
	 */
	public boolean isImmediate() {
		return immediate;
	}
	
	/**
	 * @param codeSize the codeSize to set
	 */
	public void setCodeSize(int codeSize) {
		this.codeSize = codeSize;
	}
	/**
	 * @return the codeSize
	 */
	public int getCodeSize() {
		return codeSize;
	}
	
	public int getParamAddr() {
		return getContentAddr() + getCodeSize();
	}
	

}
