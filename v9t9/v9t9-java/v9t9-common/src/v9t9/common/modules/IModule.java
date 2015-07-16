/*
  IModule.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.modules;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.List;

import v9t9.common.files.IPathFileLocator;
import v9t9.common.memory.MemoryEntryInfo;

/**
 * @author ejs
 *
 */
public interface IModule {
	URI getDatabaseURI();
	void setDatabaseURI(URI uri);
	
	String getName();
	void setName(String name);
	
//	String getImagePath();
//	void setImagePath(String imagePath);
	
	MemoryEntryInfo[] getMemoryEntryInfos();
	void addMemoryEntryInfo(MemoryEntryInfo info);
	void removeMemoryEntryInfo(MemoryEntryInfo info);
	int getMemoryEntryInfoCount();
	
	List<String> getKeywords();

	ModuleInfo getInfo();
	void setInfo(ModuleInfo info);

	Collection<File> getUsedFiles(IPathFileLocator locator);
	
	/** Identify the module uniquely by the kind of memory entries it has,
	 * but nothing specific like the module name (which sometimes must 
	 * be detected or disambiguated) or filenames (which are arbitrary). 
	 * @return
	 */
	String getMD5();
	
	/** Identify the module uniquely by the kind of memory entries it has,
	 * but nothing specific like the module name (which sometimes must 
	 * be detected or disambiguated) or filenames (which are arbitrary). 
	 * @return
	 */
	void setMD5(String moduleMd5);
	
	/**
	 * If set, this is an MD5 of one or more stock modules that may be incorrectly
	 * reported.  For example, Mini Memory and EA/8K Super Cart need to
	 * have the stored memory entries to make sense.  Vice versa, some
	 * packages may ship the RAM as ROM.
	 * @return MD5 of a module to replace with the receiver
	 */
	String getReplaceMD5();


	/**
	 * Tell whether the module starts automatically
	 */
	boolean isAutoStart();
	/**
	 * Tell whether the module starts automatically
	 */
	void setAutoStart(boolean autoStart);
	
	/**
	 * Simplify the paths in the given module, assuming the files
	 * will eventually be detected on the search path.
	 */
	void removePathsFromFiles(IPathFileLocator locator);
	
	/**
	 * Remove filenames from the module (except database URI),
	 * ensuring that file MD5s are in place.
	 * @param fileLocator 
	 */
	void simplifyContent(IPathFileLocator fileLocator);

	/**
	 * Make a deep copy of this module.
	 * @return
	 */
	IModule copy();
}
