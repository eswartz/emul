/*
  IMemoryEntry.java

  (c) 2011-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.memory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import v9t9.common.events.IEventNotifier;
import ejs.base.properties.IPersistable;
import ejs.base.settings.ISettingSection;
import ejs.base.utils.Pair;


/**
 * @author ejs
 *
 */
public interface IMemoryEntry extends IMemoryAccess, Comparable<IMemoryEntry>, IPersistable {

	String toString();

	boolean isVolatile();

	void setVolatile(boolean isVolatile);

	boolean isStatic();

	void setArea(IMemoryArea area);

	/** Save entry, if applicable 
	 * @throws IOException */
	void save() throws IOException;

	/** Load entry, if applicable 
	 * @throws IOException TODO*/
	void load() throws IOException;

	/** Unload entry, if applicable */
	void unload();

	void loadMemory(IEventNotifier notifier, ISettingSection section) throws IOException;

	/** Load symbols from file in the form:
	 * 
	 * &lt;addr&gt; &lt;name&gt;
	 * @throws IOException 
	 */
	void loadSymbolsAndClose(InputStream is) throws IOException;

	/**
	 * @param fos
	 */
	void writeSymbols(PrintStream os);

	void defineSymbol(int addr, String name);

	Integer findSymbol(String name );
	String lookupSymbol(short addr);

	void clearSymbols();

	/**
	 * @param domain
	 */
	void copySymbols(IMemoryDomain domain);

	Pair<String, Short> lookupSymbolNear(short addr, int range);

	IMemoryDomain getDomain();

	/**
	 * Get the active area.
	 * @return
	 */
	IMemoryArea getArea();

	/**
	 * Get the mapping for the address
	 * @param addr
	 * @return
	 */
	int mapAddress(int addr);

	boolean hasReadAccess();

	boolean hasWriteAccess();

	byte getLatency(int addr);

	String getName();

	String getUniqueName();

	boolean contains(int addr);

	IMemory getMemory();

	int getAddr();

	int getSize();

	boolean isWordAccess();

	int getAddrOffset();
	void onUnmap();
	void onMap();

	/**
	 * 
	 */
	void reset();

	/**
	 * Write to memory that may be read-only
	 * @param addr
	 * @param value
	 * @return true if changed
	 */
	boolean patchWord(int addr, short value);

}