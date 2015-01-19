/*
  Catalog.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;

import java.util.List;

/**
 * A catalog from a disk device
 * @author ejs
 *
 */
public class Catalog {
	public String deviceName;
	public final String volumeName;
	public final int totalSectors;
	public final int usedSectors;
	
	public final List<CatalogEntry> entries;
	private IEmulatedDisk disk;

	public Catalog(IEmulatedDisk disk, String deviceName, String volumeName, int totalSectors, int usedSectors,
			List<CatalogEntry> entries) {
		this.disk = disk;
		this.deviceName = deviceName;
		this.volumeName = volumeName;
		this.totalSectors = totalSectors;
		this.usedSectors = usedSectors;
		this.entries = entries;
	}

	public List<CatalogEntry> getEntries() {
		return entries;
	}

	public boolean isValid() {
		return totalSectors > 0 && usedSectors >= 0;
	}

	public CatalogEntry findEntry(String name) {
		for (CatalogEntry ent : entries) {
			if (ent.fileName.equalsIgnoreCase(name)) {
				return ent;
			}
		}
		return null;
	}
	
	public CatalogEntry findEntry(String name, String type, int reclen) {
		for (CatalogEntry ent : entries) {
			if (ent.fileName.equalsIgnoreCase(name)) {
				if (ent.type.equals(type) && ent.recordLength == reclen) {
					return ent;
				}
			}
		}
		return null;
	}

	public IEmulatedDisk getDisk() {
		return disk;
	}
	
	
}
