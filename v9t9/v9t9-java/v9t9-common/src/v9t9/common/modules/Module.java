/*
  Module.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.modules;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import v9t9.common.memory.MemoryEntryInfo;

/**
 * @author ejs
 *
 */
public class Module implements IModule {

	private List<MemoryEntryInfo> entries = new ArrayList<MemoryEntryInfo>();
	private String name;
	private String imagePath;
	private URI databaseURI;
	
	// not used for equality
	private List<String> keywords = new ArrayList<String>(1);
	
	public Module(URI uri, String name) {
		this.databaseURI = uri;
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((databaseURI == null) ? 0 : databaseURI.hashCode());
		result = prime * result + ((entries == null) ? 0 : entries.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Module other = (Module) obj;
		if (databaseURI == null) {
			if (other.databaseURI != null)
				return false;
		} else if (!databaseURI.equals(other.databaseURI))
			return false;
		if (entries == null) {
			if (other.entries != null)
				return false;
		} else if (!entries.equals(other.entries))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Module: " + name + " (" + entries.size() + " entries)";
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModule#getDatabaseURI()
	 */
	@Override
	public URI getDatabaseURI() {
		return databaseURI;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.modules.IModule#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModule#getImageURL()
	 */
	@Override
	public String getImagePath() {
		return imagePath;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.modules.IModule#getEntries()
	 */
	public MemoryEntryInfo[] getMemoryEntryInfos() {
		return (MemoryEntryInfo[]) entries.toArray(new MemoryEntryInfo[entries.size()]);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModule#addMemoryEntryInfo(v9t9.common.memory.MemoryEntryInfo)
	 */
	@Override
	public void addMemoryEntryInfo(MemoryEntryInfo info) {
		entries.add(info);
	}
	
	public void setMemoryEntryInfos(List<MemoryEntryInfo> entries) {
		this.entries = entries;
	}

	public void setImagePath(String image) {
		this.imagePath = image;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModule#getKeywords()
	 */
	@Override
	public List<String> getKeywords() {
		return keywords;
	}
}
