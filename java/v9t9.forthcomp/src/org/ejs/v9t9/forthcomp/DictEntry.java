/**
 * 
 */
package org.ejs.v9t9.forthcomp;

import java.util.LinkedHashMap;
import java.util.Map;

import org.ejs.v9t9.forthcomp.words.LocalVariable;
import org.ejs.v9t9.forthcomp.words.LocalVariableAddr;
import org.ejs.v9t9.forthcomp.words.StoreLocalVariable;
import org.ejs.v9t9.forthcomp.words.TargetContext;

/**
 * @author ejs
 *
 */
public class DictEntry implements Comparable<DictEntry> {


	private int link;
	private int addr;
	private String name;
	private int headerSize;
	private int codeSize;
	private boolean hidden;
	private boolean immediate;
	private boolean export;
	private boolean isDoesWord;

	private int uses;
	private Map<String, LocalVariableTriple> locals;
	private Map<String, IWord> localDict;
	private IWord hostBehavior;
	private int hostStackCount;
	private boolean targetOnly;
	
	
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
	
	public boolean isHidden() {
		return hidden;
	}
	
	public void setHostBehavior(int count, IWord hostBehavior) {
		this.hostStackCount = count;
		this.hostBehavior = hostBehavior;
	}
	
	public IWord getHostBehavior() {
		return hostBehavior;
	}

	public void setTargetOnly(boolean targetOnly) {
		this.targetOnly = targetOnly;
	}
	
	public boolean isTargetOnly() {
		return targetOnly;
	}
	
	/**
	 * @return the hostStackCount
	 */
	public int getHostStackCount() {
		return hostStackCount;
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
	

	public void setImmediate(TargetContext targetContext, boolean b) {
		this.immediate = b;
		writeEntry(targetContext);
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

	/**
	 * @param export
	 */
	public void setExport(boolean export) {
		this.export = export;
	}
	/**
	 * @return the export
	 */
	public boolean isExport() {
		return export;
	}
	
	/**
	 * @return the uses
	 */
	public int getUses() {
		return uses;
	}
	public void use() {
		uses++;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(DictEntry o) {
		return name.compareTo(o.getName());
	}

	public void allocateLocals() {
		if (locals == null) {
			locals = new LinkedHashMap<String, LocalVariableTriple>();
		}
		if (localDict == null) {
			localDict = new LinkedHashMap<String, IWord>();
		}
	}
	public int defineLocal(String name) throws AbortException {

		if (locals.containsKey(name))
			throw new AbortException(name +" already used");
		
		int index = locals.size();
		
		LocalVariable local = new LocalVariable(index);
		StoreLocalVariable storeLocal = new StoreLocalVariable(index);
		LocalVariableAddr localAddr = new LocalVariableAddr(index);
		
		locals.put(name.toUpperCase(), new LocalVariableTriple(local, storeLocal, localAddr));
		
		localDict.put(name.toUpperCase() + "!", storeLocal);
		localDict.put("'" + name.toUpperCase(), localAddr);
		localDict.put(name.toUpperCase(), local);

		return index;
	}

	public int getLocalCount() {
		return locals.size();
	}
	/**
	 * @param name
	 * @return
	 */
	public LocalVariableTriple findLocal(String name) {
		if (locals == null)
			return null;
		LocalVariableTriple triple = locals.get(name.toUpperCase());
		return triple;
	}

	/**
	 * @return
	 */
	public boolean hasLocals() {
		return locals != null;
	}

	/**
	 * @param token
	 * @return
	 */
	public IWord findLocalWord(String token) {
		return localDict.get(token.toUpperCase());
	}


	public void use(int count) {
		uses += count;
	}


	public boolean isDoesWord() {
		return isDoesWord;
	}

	public void setDoesWord(boolean isDoesWord) {
		this.isDoesWord = isDoesWord;
	}


}
