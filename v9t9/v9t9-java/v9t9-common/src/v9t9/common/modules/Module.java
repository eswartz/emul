/*
  Module.java

  (c) 2010-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.modules;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import v9t9.common.files.IPathFileLocator;
import v9t9.common.files.MD5FilterAlgorithms;
import v9t9.common.memory.MemoryEntryInfo;
import ejs.base.utils.TextUtils;

/**
 * @author ejs
 *
 */
public class Module implements IModule {
	private static Logger log = Logger.getLogger(Module.class);
	
	private List<MemoryEntryInfo> entries = new ArrayList<MemoryEntryInfo>();
	private String name;
	private URI databaseURI;
	
	// not used for equality
	private List<String> keywords = new ArrayList<String>(1);
	private ModuleInfo info;
	private String md5;
	private boolean isAutoStart;

	private String replaceMd5;
//	private String imagePath;
	
	public Module(URI uri, String name) {
		this.databaseURI = uri;
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((databaseURI == null) ? 0 : databaseURI.hashCode());
		result = prime * result + getMD5().hashCode();
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
		if (!getMD5().equals(other.getMD5()))
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
		return "Module: " + name + " = " + md5 + " (" + entries.size() + " entries)\n" + entries;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModule#getDatabaseURI()
	 */
	@Override
	public URI getDatabaseURI() {
		return databaseURI;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModule#setDatabaseURI(java.net.URI)
	 */
	@Override
	public void setDatabaseURI(URI uri) {
		databaseURI = uri;
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
	 * @see v9t9.engine.modules.IModule#getEntries()
	 */
	public MemoryEntryInfo[] getMemoryEntryInfos() {
		return (MemoryEntryInfo[]) entries.toArray(new MemoryEntryInfo[entries.size()]);
	}
	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModule#getMemoryEntryInfoCount()
	 */
	@Override
	public int getMemoryEntryInfoCount() {
		return entries.size();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModule#addMemoryEntryInfo(v9t9.common.memory.MemoryEntryInfo)
	 */
	@Override
	public void addMemoryEntryInfo(MemoryEntryInfo info) {
		entries.add(info);
		this.md5 = null;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModule#removeMemoryEntryInfo(v9t9.common.memory.MemoryEntryInfo)
	 */
	@Override
	public void removeMemoryEntryInfo(MemoryEntryInfo info) {
		entries.remove(info);
		this.md5 = null;
	}
	
	public void setMemoryEntryInfos(List<MemoryEntryInfo> entries) {
		this.entries = entries;
		this.md5 = null;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModule#getKeywords()
	 */
	@Override
	public List<String> getKeywords() {
		return keywords;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModule#getInfo()
	 */
	@Override
	public ModuleInfo getInfo() {
		return info;
	}
	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModule#getInfo(v9t9.common.modules.ModuleInfo)
	 */
	@Override
	public void setInfo(ModuleInfo info) {
		this.info = info;
		
	}
	
//	/* (non-Javadoc)
//	 * @see v9t9.common.modules.IModule#getImagePath()
//	 */
//	@Override
//	public String getImagePath() {
//		return imagePath;
//	}
//	/* (non-Javadoc)
//	 * @see v9t9.common.modules.IModule#setImagePath(java.lang.String)
//	 */
//	@Override
//	public void setImagePath(String imagePath) {
//		this.imagePath = imagePath;
//		
//	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModule#getUsedFiles()
	 */
	@Override
	public Collection<File> getUsedFiles(IPathFileLocator locator) {
		Set<File> files = new TreeSet<File>();
		for (MemoryEntryInfo info : getMemoryEntryInfos()) {
			File file;
			file = getFileFrom(locator, info.getFilename());
			if (file != null)
				files.add(file);
			file = getFileFrom(locator, info.getFilename2());
			if (file != null)
				files.add(file);
		}
		return files;
	}

	/**
	 * @param locator
	 * @param filename2
	 * @return
	 */
	private File getFileFrom(IPathFileLocator locator, String filename) {
		if (TextUtils.isEmpty(filename))
			return null;
		URI uri = locator.findFile(filename);
		if (uri == null)
			return null;
		try {
			return new File(uri);
		} catch (IllegalArgumentException e) {
			if (uri.getScheme().equals("jar")) {
				uri = URI.create(uri.getSchemeSpecificPart());
				if ("file".equals(uri.getScheme())) {
					String ssp = uri.getSchemeSpecificPart();
					int idx = ssp.lastIndexOf('!');
					if (idx >= 0)
						ssp = ssp.substring(0, idx);
					return new File(ssp);
				}
			}
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModule#getMD5()
	 */
	@Override
	public String getMD5() {
		if (md5 == null) {
			md5 = ModuleMD5Sums.createMD5(this);
		}
		return md5;
	}

	/**
	 * @param moduleMd5
	 */
	@Override
	public void setMD5(String moduleMd5) {
		this.md5 = moduleMd5;
	}
	
	@Override
	public String getReplaceMD5() {
		return replaceMd5;
	}
	
	public void setReplaceMD5(String replaceMd5) {
		this.replaceMd5 = replaceMd5;
	}
	

	@Override
	public boolean isAutoStart() {
		return isAutoStart;
	}
	
	
	@Override
	public void setAutoStart(boolean autoStart) {
		this.isAutoStart = autoStart;
		
	}

	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModule#simplifyPaths(v9t9.common.files.IPathFileLocator)
	 */
	@Override
	public void removePathsFromFiles(IPathFileLocator locator) {
		for (MemoryEntryInfo info : getMemoryEntryInfos()) {
			shortenPath(locator, info, MemoryEntryInfo.FILENAME);
			shortenPath(locator, info, MemoryEntryInfo.FILENAME2);
		}
	}

	/**
	 * @param info
	 * @param prop
	 */
	private void shortenPath(IPathFileLocator locator, MemoryEntryInfo info, String prop) {
		Object path = info.getProperties().get(prop);
		if (path == null)
			return;
		String replPathStr = null; 
		String pathStr = path.toString();
		for (URI searchURI : locator.getSearchURIs()) {
			String searchStr = searchURI.toString();
			// remove e.g. file:/foo/bar from jar:file:/foo/bar
			String newPathStr = pathStr.replace(searchStr, "");
			if (!newPathStr.equals(pathStr)) {
				if (replPathStr == null || newPathStr.length() < replPathStr.length()) {
					replPathStr = newPathStr;
					info.getProperties().put(prop, newPathStr);
				}
			} else {
				int idx = searchStr.indexOf(':');
				if (idx > 0) {
					// remove e.g. /foo/bar from file:/foo/bar
					searchStr = searchStr.substring(idx+1);
					newPathStr = pathStr.replace(searchStr, "");
					if (!newPathStr.equals(pathStr)) {
						if (replPathStr == null || newPathStr.length() < replPathStr.length()) {
							replPathStr = newPathStr;
							info.getProperties().put(prop, newPathStr);
						}
					}
				}
				
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModule#removeFilenames()
	 */
	@Override
	public void simplifyContent(IPathFileLocator locator, boolean removeFilenames) {
		for (MemoryEntryInfo info : getMemoryEntryInfos()) {
			if (!info.isStored()) {
				ensureFileMD5(locator, info, removeFilenames, 
						MemoryEntryInfo.FILENAME, 
						MemoryEntryInfo.FILE_MD5, MemoryEntryInfo.FILE_MD5_ALGORITHM);
				ensureFileMD5(locator, info, removeFilenames,
						MemoryEntryInfo.FILENAME2, 
						MemoryEntryInfo.FILE2_MD5, MemoryEntryInfo.FILE2_MD5_ALGORITHM);
			}
		}
		
	}

	/**
	 * @param locator
	 * @param removeFilenames 
	 * @param info2
	 * @param filename2
	 * @param file2Md5
	 */
	private void ensureFileMD5(IPathFileLocator locator, MemoryEntryInfo info,
			boolean removeFilenames, String filenameProp, String fileMd5Prop, String md5AlgProp) {
		Object filename = info.getProperties().get(filenameProp);
		if (filename != null) {
			if (info.getProperties().get(fileMd5Prop) == null) {
				try {
					URI uri = locator.findFile(filename.toString());
					if (uri == null) {
						uri = locator.createURI(filename.toString());
					}
					if (uri == null) {
						log.error(getName() + ": could not find " + info.getFilename());
						return;
					}
					
					String alg;
					Object algObj = info.getProperties().get(md5AlgProp);
					if (algObj == null)
						alg = info.getDefaultMD5Algorithm();
					else
						alg = algObj.toString();
					
					String md5 = locator.getContentMD5(uri,
							MD5FilterAlgorithms.create(alg));
					
					if (md5.isEmpty())
						log.error(getName() + ": could not fetch MD5 for " + info.getFilename());
					else
						info.getProperties().put(MemoryEntryInfo.FILE_MD5, md5);
				} catch (URISyntaxException e) {
					log.error(getName() + ": could not fetch " + info.getFilename(), e);
				} catch (IOException e) {
					log.error(getName() + ": no MD5 for " + info.getFilename(), e);
				}
			}
			if (removeFilenames) {
				if (info.getProperties().get(fileMd5Prop) != null)
					info.getProperties().remove(filenameProp);
			}
		}

	}

	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModule#copy()
	 */
	@Override
	public IModule copy() {
		Module copy = new Module(databaseURI, name);
		copy.keywords.addAll(keywords);
		if (info != null)
			copy.info = info.copy();
		copy.md5 = md5;
		copy.replaceMd5 = replaceMd5;
		copy.isAutoStart = isAutoStart;
		for (MemoryEntryInfo info : entries) {
			copy.entries.add(new MemoryEntryInfo(info.getProperties()));
		}
		return copy;
	}
}
