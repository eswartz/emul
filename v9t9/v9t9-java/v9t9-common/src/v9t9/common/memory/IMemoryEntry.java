/*
  IMemoryEntry.java

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
package v9t9.common.memory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import ejs.base.properties.IPersistable;
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

	/** Load entry, if applicable */
	void load();

	/** Unload entry, if applicable */
	void unload();

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

	byte getLatency();

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

}