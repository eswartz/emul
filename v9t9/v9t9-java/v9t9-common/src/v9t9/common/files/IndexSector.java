/**
 * 
 */
package v9t9.common.files;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ejs
 *
 */
public class IndexSector {

	public static IndexSector create(byte[] sec1) {
		IndexSector index = new IndexSector();

		for (int i = 0; i < sec1.length; i += 2) {
			int sec = ((sec1[i + 0] & 0xff) << 8) | (sec1[i + 1] & 0xff);
			if (sec != 0)
				index.entries.add(sec);
		}
		
		return index;
	}

	private List<Integer> entries;

	public IndexSector() {
		entries = new ArrayList<Integer>();
	}
	
	public byte[] toBytes() {
		byte[] sec = new byte[256];
		int i = 0;
		for (Integer n : entries) {
			sec[i] = (byte) (n >> 8);
			sec[i + 1] = (byte) (int) n;
			i += 2;
		}
		return sec;
	}

	public void remove(int indexSector) {
		entries.remove((Integer) indexSector);
	}
	public void add(int indexSector, String fileName, Catalog prevCatalog) throws IOException {
		CatalogEntry exist = prevCatalog.findEntry(fileName);
		if (exist != null) {
			int existIndex = entries.indexOf(exist.indexSector);
			if (existIndex < 0) 
				throw new IOException("inconsistent directory:  entry " + exist + " not in index sector");
			// replace
			entries.set(existIndex, indexSector);
			return;
		}

		if (entries.size() >= 128) {
			throw new IOException("directory full");
		}
		
		// insert in order
		for (CatalogEntry entry : prevCatalog.getEntries()) {
			if (entry.getFile().getFileName().compareTo(fileName) > 0) {
				int entIndex = entries.indexOf(entry.indexSector);
				if (entIndex < 0) 
					throw new IOException("inconsistent directory:  entry " + exist + " not in index sector");
				// insert
				entries.add(entIndex, indexSector);
				return;
			}
		}
		entries.add(indexSector);
	}
}
