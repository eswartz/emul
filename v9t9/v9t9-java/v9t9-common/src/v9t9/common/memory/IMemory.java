/*
  IMemory.java

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

import ejs.base.properties.IPersistable;

/**
 * @author ejs
 *
 */
public interface IMemory extends IPersistable {

	IMemoryEntryFactory getMemoryEntryFactory();
	void setMemoryEntryFactory(IMemoryEntryFactory factory);
	
	void addListener(IMemoryListener listener);

	void removeListener(IMemoryListener listener);

	void notifyListenersOfPhysicalChange(IMemoryEntry entry);

	void notifyListenersOfLogicalChange(IMemoryEntry entry);

	void addDomain(String key, IMemoryDomain domain);

	IMemoryDomain getDomain(String key);

	void addAndMap(IMemoryEntry entry);

	void removeAndUnmap(IMemoryEntry entry);

	void setModel(IMemoryModel model);
	
	IMemoryModel getModel();

	IMemoryDomain[] getDomains();

	/**
	 * 
	 */
	void save();

	/**
	 * 
	 */
	void clear();

}