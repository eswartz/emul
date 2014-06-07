/*
  DictEntry.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp;

import java.util.LinkedHashMap;
import java.util.Map;

import v9t9.tools.forthcomp.words.LocalVariable;

/**
 * @author ejs
 *
 */
public class DictEntry implements Comparable<DictEntry> {


	protected int link;
	protected int addr;
	protected int endAddr;

	
	protected String name;
	protected int headerSize;
	protected int codeSize;
	protected boolean hidden;
	protected boolean immediate;
	protected boolean export;
	protected boolean isDoesWord;

	protected int uses;
	protected Map<String, LocalVariableTriple> locals;
	protected Map<String, IWord> localDict;
	protected IWord hostBehavior;
	protected IWord targetWord;
	protected int hostStackCount;
	protected boolean targetOnly;
	private boolean inline;
	
	
	
	/**
	 * @param name 
	 * 
	 */
	public DictEntry(int headerSize, int addr, String name) {
		this.headerSize = headerSize;
		this.addr = addr;
		this.name = name;
	}
	
	
	public void setEndAddr(int endAddr) {
		this.endAddr = endAddr;
	}

	public int getSize(ITargetContext context) {
		if (endAddr != 0)
			return endAddr - addr;
		else
			return context.getDP() - addr;
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.ITargetWord#getHeaderSize()
	 */
	public int getHeaderSize() {
		return headerSize;
	}
	
	public int getContentAddr() {
		return addr + headerSize;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.ITargetWord#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.ITargetWord#getAddr()
	 */
	public int getAddr() {
		return addr;
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.ITargetWord#getLink()
	 */
	public int getLink() {
		return link;
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.ITargetWord#setLink(int)
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
	public void setTargetWord(IWord targetWord) {
		this.targetWord = targetWord;
	}
	/**
	 * @return the targetWord
	 */
	public IWord getTargetWord() {
		return targetWord;
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
	public void writeEntry(ITargetContext targetContext) {
		byte[] ent = doWriteEntry(targetContext);
		
		for (int i = 0; i < ent.length; i++)
			targetContext.writeChar(addr + i, ent[i]);
	}


	protected byte[] doWriteEntry(ITargetContext targetContext) {
		byte[] ent = new byte[headerSize];
		int ad = 0;
		
		ad = targetContext.writeCell(ent, ad, link);
		
		int flags = (hidden ? 0 : 0x80) | (immediate ? 0x40 : 0);
		
		ent[ad++] = (byte) ((name.length() & 0x1f) | flags);
		for (int i = 0; i < name.length(); i++)
			ent[ad++] = (byte) name.charAt(i);
		
		while (ad <  headerSize)
			ent[ad++] = 32;
		
		return ent;
	}

	/**
	 * @return
	 */
	public boolean isImmediate() {
		return immediate;
	}
	

	public void setImmediate(ITargetContext targetContext, boolean b) {
		this.immediate = b;
		if (isExport())
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

	public void allocLocals() {
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
		//StoreLocalVariable storeLocal = new StoreLocalVariable(index);
		//LocalVariableAddr localAddr = new LocalVariableAddr(index);
		
		locals.put(name.toUpperCase(), new LocalVariableTriple(local));
		
		//localDict.put(name.toUpperCase() + "!", storeLocal);
		//localDict.put("'" + name.toUpperCase(), localAddr);
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


	/**
	 * @param b
	 */
	public void setInline(boolean b) {
		this.inline = b;
	}
	
	/**
	 * @return the inline
	 */
	public boolean isInline() {
		return inline;
	}


	/**
	 * @return
	 */
	public boolean canInline() {
		return getAddr() == 0 || isInline();
	}

}
