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
	public final String deviceName;
	public final String volumeName;
	public final int totalSectors;
	public final int usedSectors;
	
	public final  List<CatalogEntry> entries;

	public Catalog(String deviceName, String volumeName, int totalSectors, int usedSectors,
			List<CatalogEntry> entries) {
		this.deviceName = deviceName;
		this.volumeName = volumeName;
		this.totalSectors = totalSectors;
		this.usedSectors = usedSectors;
		this.entries = entries;
	}

	public List<CatalogEntry> getEntries() {
		return entries;
	}
	
	
}
