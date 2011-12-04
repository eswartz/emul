/**
 * 
 */
package v9t9.common.memory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import v9t9.base.properties.IPersistable;
import v9t9.base.utils.Pair;

/**
 * @author ejs
 *
 */
public interface IMemoryEntry extends IMemoryAccess, Comparable<IMemoryEntry>, IPersistable {

	String toString();

	boolean isVolatile();

	void setVolatile(boolean isVolatile);

	boolean isStatic();

	void setArea(MemoryArea area);

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
	void loadSymbols(InputStream is) throws IOException;

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