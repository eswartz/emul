package v9t9.engine.files;

import java.util.List;

/**
 * A catalog from a disk device
 * @author ejs
 *
 */
public class Catalog {
	public final String volumeName;
	public final int totalSectors;
	public final int usedSectors;
	
	public final  List<CatalogEntry> entries;

	public Catalog(String volumeName, int totalSectors, int usedSectors,
			List<CatalogEntry> entries) {
		super();
		this.volumeName = volumeName;
		this.totalSectors = totalSectors;
		this.usedSectors = usedSectors;
		this.entries = entries;
	}

	public List<CatalogEntry> getEntries() {
		return entries;
	}
	
	
}
