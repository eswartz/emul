/**
 * 
 */
package v9t9.machine.ti99.machine;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import v9t9.common.files.DataFiles;
import v9t9.common.files.FDR;
import v9t9.common.files.FDRFactory;
import v9t9.common.files.IMD5SumFilter;
import v9t9.common.files.IPathFileLocator;
import v9t9.common.files.InvalidFDRException;
import v9t9.common.files.MD5FilterAlgorithms;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.modules.IModule;
import v9t9.common.modules.IModuleDetector;
import v9t9.common.modules.IModuleManager;
import v9t9.common.modules.Module;
import v9t9.engine.memory.MemoryEntryInfoBuilder;
import v9t9.engine.memory.StdMultiBankedMemoryEntry;
import ejs.base.utils.FileUtils;
import ejs.base.utils.HexUtils;
import ejs.base.utils.StorageException;
import ejs.base.utils.StreamXMLStorage;
import ejs.base.utils.TextUtils;
import ejs.base.utils.XMLUtils;

/**
 * This class analyzes collections of files and organizes them
 * into IModule instances.
 * @author ejs
 *
 */
public class ModuleDetector implements IModuleDetector {

	private static final Logger log = Logger.getLogger(ModuleDetector.class);

	private static final String EA_8K_SUPER_CART = "EA/8K Super Cart";
	private static final String MINI_MEMORY = "Mini Memory";
	
	private URI databaseURI;
	private Map<String, IModule> modules;

	private IModuleManager moduleManager;

	private IPathFileLocator fileLocator;

	private boolean readHeaders;
	private boolean ignoreStock;

	public ModuleDetector(URI databaseURI, IPathFileLocator fileLocator, IModuleManager moduleManager) {
		this.databaseURI = databaseURI;
		this.fileLocator = fileLocator;
		this.moduleManager = moduleManager;
		this.modules = new HashMap<String, IModule>();
	}
	
	public void setReadHeaders(boolean readHeaders) {
		this.readHeaders = readHeaders;
	}
	public void setIgnoreStock(boolean ignoreStock) {
		this.ignoreStock = ignoreStock;
	}

	@Override
	public Collection<IModule> getModules() {
		List<IModule> modules = new ArrayList<IModule>(this.modules.values());
		Collections.sort(modules, new Comparator<IModule>() {

			@Override
			public int compare(IModule o1, IModule o2) {
				return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
			}
		});
		
		return modules;
	}
	
	@Override
	public Collection<IModule> scan(File base) {
		File[] files = null;
		if (base.isDirectory()) {
			files = base.listFiles();
		} else if (base.getParentFile() != null) {
			final String baseKey = getModuleBaseFile(base.getName());
			files = base.getParentFile().listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					String key = getModuleBaseFile(name);
					return key.equals(baseKey);
				}
			});
			if (files == null || !Arrays.asList(files).contains(base)) {
				return Collections.emptyList();
			}
		}
		if (files == null) {
			return Collections.emptyList();
		}
		
		Map<String, IModule> moduleMap = new HashMap<String, IModule>();
		
		for (File file : files) {
			if (file.isDirectory())
				continue;

			if (analyzeV9t9ModuleFile(databaseURI, file.toURI(), file.getAbsolutePath(), 
					getModuleBaseFile(file.getName()),
					moduleMap))
				continue;
			
			if (analyzeGRAMKrackerModuleFile(databaseURI, file, moduleMap))
				continue;
			
			if (analyzeRPKModuleFile(databaseURI, file, moduleMap))
				continue;
			
			if (analyzeZipModuleFile(databaseURI, file, moduleMap))
				continue;
		}

		// remove spurious modules
		for (Iterator<IModule> iter = moduleMap.values().iterator(); iter.hasNext(); ) {
			IModule module = iter.next();
			//System.out.println("module: " + module);
			log.info("module: " + module);
			
			String moduleName = module.getName();
			if (TextUtils.isEmpty(moduleName) || !isASCII(moduleName)) {
				iter.remove();
				continue;
			}
			
			boolean anyHeader = false;
			for (MemoryEntryInfo info :module.getMemoryEntryInfos()) {
				if (Boolean.TRUE.equals(info.getProperties().get(MemoryEntryInfo.HAS_HEADER))) {
					anyHeader = true;
				}
			}
			if (!anyHeader) {
				iter.remove();
				continue;
			}
			
//			// fixup specific modules
//			if (moduleName.equalsIgnoreCase(MINI_MEMORY)) {
//				MemoryEntryInfo[] infos = module.getMemoryEntryInfos();
//				if (infos.length == 2) {
//					MemoryEntryInfo info = MemoryEntryInfoBuilder.standardModuleRom("minir.bin")
//							.withAddress(0x7000)
//							.withSize(0x1000)
//							.storable(true)
//							.create("Mini Memory Non-Volatile RAM");
//					module.addMemoryEntryInfo(info);
//					
//					// fixup earlier entry if present
//					for (MemoryEntryInfo xinfo : infos) {
//						if (xinfo.getDomainName().equals(IMemoryDomain.NAME_CPU)) {
//							URI uri = fileLocator.findFile(xinfo.getFilename());
//							try {
//								int limit = 0x1000;
//								String md5 = fileLocator.getContentMD5(uri, 0, limit, true);
//								xinfo.getProperties().put(MemoryEntryInfo.FILE_MD5, md5);
//								xinfo.getProperties().put(MemoryEntryInfo.FILE_MD5_LIMIT, limit);
//							} catch (IOException e) {
//								e.printStackTrace();
//							}
//
//						}
//					}
//				}
//			}
//			else if (module.getName().equalsIgnoreCase(EA_8K_SUPER_CART)) {
//				MemoryEntryInfo[] infos = module.getMemoryEntryInfos();
//				if (infos.length == 1) {
//					MemoryEntryInfo info = MemoryEntryInfoBuilder.standardModuleRom("ea+8kr.bin")
//							.withAddress(0x6000)
//							.withSize(0x2000)
//							.storable(true)
//							.create("EA/8K Super Cart Non-Volatile RAM");
//					module.addMemoryEntryInfo(info);
//				}
//			}
			
			// Replace the name of any stock module
			if (!ignoreStock) {
				IModule stock = moduleManager.findStockModuleByMd5(module.getMD5());
				if (stock != null) {
					if (stock.getMemoryEntryInfos().length == module.getMemoryEntryInfos().length) {
						module.setName(stock.getName());
					} else {
						module.setName(stock.getName() + " (possibly broken)");
					}
				}
			}
		}
		
		List<IModule> modules = new ArrayList<IModule>(moduleMap.values());
		Collections.sort(modules, new Comparator<IModule>() {

			@Override
			public int compare(IModule o1, IModule o2) {
				return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
			}
		});
		
		this.modules.putAll(moduleMap);
		
		return modules;
	}

	private static final Pattern tosecNaming = Pattern.compile("(?i).*\\([^)]+\\).*\\([^)]+\\).*\\([^)]+\\).*\\(([^)]+)\\)?");

	private String getModuleBaseFile(String fname) {
		String base = getModuleFileBase(fname); 
		
		String key = base.substring(0, base.length());	
		if (Character.isLetter(key.charAt(key.length() - 1)))
			key = key.substring(0, key.length() - 1); // minus indicator char
		int idx = key.lastIndexOf('/');
		if (idx < 0)
			idx = key.lastIndexOf('\\');
		if (idx > 0)
			key = key.substring(idx+1);
		
				
		return key;

	}
	

	private char getV9t9ModuleTypePart(String fname) {
		String base = getModuleFileBase(fname);

		return base.charAt(base.length() - 1);
	}

	/**
	 * @param fname
	 * @return
	 */
	protected String getModuleFileBase(String fname) {
		String base = fname;
		int idx = fname.lastIndexOf('.');
		if (idx > 0)
			base = fname.substring(0, idx);
		

		// name may be from tosec archive, and the interesting part is
		// the last parenthesized group
		Matcher m = tosecNaming.matcher(base);
		if (m.matches()) {
			base = m.group(1);
		}
		
		return base;
	}
	
	/**
	 * Look for V9t9 module parts -- bare binaries with pieces identified
	 * by the final filename letter ('C' = console module ROM, 'D' = console module ROM,
	 * bank #2, 'G' = module GROM). 
	 */
//	private boolean analyzeV9t9ModuleFile(URI databaseURI, URI uri,
//			String fname, String key,
//			Map<String, IModule> moduleMap) {
//		if (!fname.toLowerCase().endsWith(".bin"))
//			return false;
//		
////		String key = getModuleBaseFile(fname);
//		
//		return analyzeV9t9ModuleFile(databaseURI, uri, fname, key, moduleMap);
//	}

	/**
	 * Look for V9t9 module parts -- bare binaries with pieces identified
	 * by the final filename letter ('C' = console module ROM, 'D' = console module ROM,
	 * bank #2, 'G' = module GROM). 
	 */
	private boolean analyzeV9t9ModuleFile(URI databaseURI, URI uri,
			String fname, String key,
			Map<String, IModule> moduleMap) {
		if (!fname.toLowerCase().endsWith(".bin"))
			return false;
		
		byte[] content = null;
		try {
			try {
				File file = new File(uri);
				try {
					FDR fdr = FDRFactory.createFDR(file);
					if (fdr != null && isASCII(fdr.getFileName())) {
						fdr.validate();
						// hmm, likely a file, and not a module
						log.debug("Not treating " + fname + " as module since it looks like a file: " + fdr);
						return false;
					}
				} catch (InvalidFDRException e) {
					// okay, not likely an errant disk image
				}
				content = DataFiles.readMemoryImage(file, 0, 0x10000);
			} catch (IllegalArgumentException e) {
				// not file
				content = FileUtils.readInputStreamContentsAndClose(uri.toURL().openStream());
			}
		} catch (IOException e) {
			return false;
		}

		String domain = null;

		char last = getV9t9ModuleTypePart(fname);
		switch (last) {
		case 'C':
		case 'c':
		case 'D':
		case 'd':
			domain = IMemoryDomain.NAME_CPU;
			break;

		case 'G':
		case 'g':
			domain = IMemoryDomain.NAME_GRAPHICS;
			break;
		
		default:
			log.debug("Unknown file naming for " + fname);
			if (looksLikeBankedOr9900Code(content)) {
				log.debug("Assuming content is module ROM");
				domain = IMemoryDomain.NAME_CPU; 
			} else {
				log.debug("Assuming content is module GROM");
				domain = IMemoryDomain.NAME_GRAPHICS; 
			}
			break;
		}
		
		
		int baseAddr = 0x6000;
		String moduleName = readHeaderName(fname, content, baseAddr, domain);
	
		if (moduleName == null) {
			if (domain == IMemoryDomain.NAME_GRAPHICS) {
				baseAddr = 0x2000;
				moduleName = readHeaderName(fname, content, baseAddr, domain);
			}
		}
		if (moduleName == null) {
			return false;
		}
		
		log.debug("Module Name: " + moduleName);
		
		IModule module = moduleMap.get(key);
		if (module == null) {
			module = new Module(databaseURI, key);
		}
		if (moduleName.length() > 0) {
			// HACK
			if ("Easy Bug".equalsIgnoreCase(moduleName)) {
				moduleName = MINI_MEMORY;
			}
			module.setName(moduleName);
		}

		MemoryEntryInfo info = null;

		if (domain.equals(IMemoryDomain.NAME_CPU)) {
			switch (last) {
			case 'C':
			case 'c':
			default:
				info = injectModuleRom(module, databaseURI, moduleName, fname, 0, content.length);
				break;
			case 'D':
			case 'd':
				info = injectModuleBank2Rom(module, databaseURI, moduleName, fname, 0);
				break;
			}
		} else {
			if (baseAddr == 0x6000)
				info = injectModuleGrom(module, databaseURI, moduleName, fname, 0);
			else {
				info = MemoryEntryInfoBuilder.standardModuleGrom(fname)
						.withAddress(baseAddr)
						.withOffset(0)
						.withSize(-0xA000)
						.create(moduleName);
				fetchMD5(module, info, false);
				module.addMemoryEntryInfo(info);
			}
		}
		
		if (info != null && hasId(content)) {
			info.getProperties().put(MemoryEntryInfo.HAS_HEADER, true);
		}
		
		// register/update module
		moduleMap.put(key, module);

		return true;
	}

	/**
	 * @param content
	 * @return
	 */
	private boolean hasId(byte[] content) {
		for (int addr = 0 ; addr < content.length; addr += 0x2000) {
			if (content[addr] == (byte) 0xaa) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param content
	 * @return
	 */
	private boolean looksLikeBankedOr9900Code(byte[] content) {
		int insts = 0;
		
		// assume that real content is in the first half (e.g. for E/A where
		// content for console is in latter half)
		for (int addr = 0; addr < content.length / 2; addr += 2) {
			int word = readAddr(content, addr);
			if (word == 0x45b /* RT */ 
					|| word == 0x8300  /* CPU RAM base */
					|| word == 0x83e0  /* GPLWS */
					|| word == 0x8c00  /* VDPWA */
					|| word == 0x8c02  /* VDPWD */
					|| word == 0x8400  /* SOUND */
					|| word == 0x380)  /* RTWP */
				{
				insts++;
			}
		}
		
		boolean allIded = true;
		for (int addr = 0 ; addr < content.length; addr += 0x2000) {
			if (content[addr] != (byte) 0xaa) {
				allIded = false;
			}
		}
		if (content.length > 0x2000 && allIded)
			insts *= 2;
		
		boolean isROMCode = insts > content.length / 256;
		
		log.debug("# insts = " + insts +"; all banks have IDs: " + allIded);
		
		return isROMCode;
	}

	private MemoryEntryInfo injectModuleGrom(IModule module, URI databaseURI,
			String moduleName, String fileName, int fileOffset) {
		MemoryEntryInfo info = MemoryEntryInfoBuilder.standardModuleGrom(fileName)
				.withOffset(fileOffset)
				.withSize(-0xA000)
				.create(moduleName);
		fetchMD5(module, info, false);
		module.addMemoryEntryInfo(info);
		return info;
	}

	private void fetchMD5(IModule module, MemoryEntryInfo info, boolean bank2) {
		String md5;
		try {
			String fileName = bank2 ? info.getFilename2() : info.getFilename();
			URI uri = fileLocator.findFile(fileName);
			if (uri == null)
				return;
			
			IMD5SumFilter filter;
			if (IMemoryDomain.NAME_GRAPHICS.equals(info.getDomainName())) {
				filter = MD5FilterAlgorithms.GromFilter.INSTANCE;
			} else {
				filter = MD5FilterAlgorithms.FullContentFilter.INSTANCE;
			}
			md5 = fileLocator.getContentMD5(uri, filter, true);
			
			if (bank2) {
				info.getProperties().put(MemoryEntryInfo.FILE2_MD5, md5);
			} else {
				info.getProperties().put(MemoryEntryInfo.FILE_MD5, md5);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private MemoryEntryInfo injectModuleRom(IModule module, URI databaseURI,
			String moduleName, String fileName, int fileOffset, int fileSize) {
		MemoryEntryInfo info = null;
		boolean found = false;
		for (MemoryEntryInfo ex : module.getMemoryEntryInfos()) {
			// modify a banked entry
			if (ex.isBanked() && ex.getAddress() == 0x6000 && IMemoryDomain.NAME_CPU.equals(ex.getDomainName())) {
				ex.getProperties().put(MemoryEntryInfo.FILENAME, fileName);
				ex.getProperties().put(MemoryEntryInfo.CLASS, StdMultiBankedMemoryEntry.class);
				ex.getProperties().put(MemoryEntryInfo.OFFSET, fileOffset);
				ex.getProperties().put(MemoryEntryInfo.SIZE, -0x2000);
				fetchMD5(module, ex, false);
				found = true;
				info = ex;
				break;
			} 
		}
		if (!found) {
			if (fileSize > 0x3e00) {
				// probably a new-format reversed PBX module
				info = MemoryEntryInfoBuilder.standardModuleRom(fileName)
						.withOffset(fileOffset)
						.withBankClass(StdMultiBankedMemoryEntry.class)
						.withSize(-0x2000)
						.isReversed(true)
						.create(moduleName);
			} else {
				info = MemoryEntryInfoBuilder.standardModuleRom(fileName)
						.withOffset(fileOffset)
						.withSize(-0x2000)
						.create(moduleName);
			}
			
			fetchMD5(module, info, false);
			module.addMemoryEntryInfo(info);
		}
		return info;
	}

	private MemoryEntryInfo injectModuleBank2Rom(IModule module, URI databaseURI,
			String moduleName, String fileName, int fileOffset) {
		MemoryEntryInfo info = null;
		boolean found = false;
		for (MemoryEntryInfo ex : module.getMemoryEntryInfos()) {
			// modify a non-banked entry
			if (ex.getAddress() == 0x6000 && IMemoryDomain.NAME_CPU.equals(ex.getDomainName())) {
				ex.getProperties().put(MemoryEntryInfo.FILENAME2, fileName);
				ex.getProperties().put(MemoryEntryInfo.CLASS, StdMultiBankedMemoryEntry.class);
				ex.getProperties().put(MemoryEntryInfo.SIZE, -0x2000);
				ex.getProperties().put(MemoryEntryInfo.OFFSET, fileOffset);
				fetchMD5(module, ex, true);
				found = true;
				info = ex;
				break;
			} 
		}
		if (!found) {
			// make temporary
			info = MemoryEntryInfoBuilder.standardModuleRom(fileName)
					.withFilename2(fileName)
					.withOffset2(fileOffset)
					.withBankClass(StdMultiBankedMemoryEntry.class)
					.create(moduleName);
			fetchMD5(module, info, true);
			module.addMemoryEntryInfo(info);
		}
		return info;
		
	}
	
	
	/**
	 * Analyze GRAM Kracker-format files
	 * 
	 * &lt;http://www.ninerpedia.org/index.php/GRAM_Kracker_format&gt;
	 *  
	 * @param databaseURI 
	 * @param file
	 * @param moduleMap
	 * @return
	 */
	private boolean analyzeGRAMKrackerModuleFile(URI databaseURI, File file,
			Map<String, IModule> moduleMap) {
		
		
		return false;
	}
	
	
	/**
	 * Analyze MESS *.rpk format files
	 *  
	 * @param databaseURI 
	 * @param file
	 * @param moduleMap
	 * @return
	 */
	private boolean analyzeRPKModuleFile(URI databaseURI, File file,
			Map<String, IModule> moduleMap) {
		
		if (!file.getName().toLowerCase().endsWith(".rpk")) {
			return false;
		}

		log.debug("Trying " + file + " as RPK");

		
		// it is a zip container: softlist.xml contains the info
		ZipFile zf;
		try {
			zf = new ZipFile(file);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		try {
			IModule module = null;
			ZipEntry softlist = zf.getEntry("softlist.xml");
			if (softlist != null) {
				log.debug("Handling softlist.xml");
				InputStream is = zf.getInputStream(softlist);
				try {
					module = convertSoftList(databaseURI, file.toURI(), is);
				} finally {
					is.close();
				}
			}
			else {
				// try layout.xml + meta-inf.xml
				ZipEntry layout = zf.getEntry("layout.xml");
				ZipEntry metainf = zf.getEntry("meta-inf.xml");
				if (layout != null && metainf != null) {
					InputStream lis = null;
					InputStream mis = null;
					try {
						lis = zf.getInputStream(layout);
						mis = metainf != null ? zf.getInputStream(metainf) : null;
						module = convertLayoutMetainf(databaseURI, file.toURI(), lis, mis);
					} finally {
						if (lis != null)
							lis.close();
						if (mis != null)
							mis.close();
					}
					
				}
			}
			
			if (module != null) {
				moduleMap.put(file.getName(), module);
				
				if (readHeaders) {
					// get a real name and detect auto-start modules
					for (MemoryEntryInfo info : module.getMemoryEntryInfos()) {
						String name = readHeaderName(module.getName(), zf, info.getFilename(), info.getDomainName());
						if (name != null && !name.isEmpty()) {
							if (!name.equals(module.getName())) {
								System.out.println("--> " + module.getName() + "; MD5=" + module.getMD5());
								module.setName(name);
							}
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				zf.close();
			} catch (IOException e) {
			}
		}
		
		return true;
	}
	
	/**
	 * Analyze *.zip files
	 *  
	 * @param databaseURI 
	 * @param file
	 * @param moduleMap
	 * @return
	 */
	private boolean analyzeZipModuleFile(URI databaseURI, File file,
			Map<String, IModule> moduleMap) {
		
		ZipFile zf;
		try {
			zf = new ZipFile(file);
		} catch (Exception e) {
			return false;
		}
		
		boolean any = false;
		try {
			boolean oneModule = zf.size() <= 4;
			log.debug("Assuming " + file + " has " + (oneModule ? "one module" :"multiple modules"));
			
			Map<String, IModule> newModules = new HashMap<String, IModule>();
			for (Enumeration<? extends ZipEntry> en = zf.entries(); en.hasMoreElements(); ) {
				ZipEntry ent = en.nextElement();
				
				try {
					URI uri = makeZipUri(file.toURI(), ent.getName());
					
					if (oneModule) {
						// associate with zip file's module
						if (analyzeV9t9ModuleFile(databaseURI, uri, uri.toString(), file.getName(), newModules)) {
							log.debug("Matched " + ent.getName() + " from " + file.getName());
							// nice
						}
					} else {
						// associate with appropriate module
						if (analyzeV9t9ModuleFile(databaseURI, uri, uri.toString(), file.getName(), newModules)) {
							log.debug("Matched " + ent.getName() + " from " + file.getName());
							// nice
						}
					}
				} catch (URISyntaxException e) {
					continue;
				}
				
				any = true;
				
				
			}
			
			if (!newModules.isEmpty()) {
				moduleMap.putAll(newModules);
				
				for (IModule module : newModules.values()) {
					// turn this on to get a real name and detect auto-start modules
					for (MemoryEntryInfo info : module.getMemoryEntryInfos()) {
						String name = readHeaderName(module.getName(), zf, info.getFilename(), info.getDomainName());
						if (name != null && !name.isEmpty()) {
							if (!name.equals(module.getName())) {
								System.out.println("--> " + module.getName() + "; MD5=" + module.getMD5());
								module.setName(name);
							}
						}
					}
				}
			}
		
		} finally {
			try {
				zf.close();
			} catch (IOException e) {
			}
		}
		
		return any;
	}
	
	/**
	 * @param is
	 * @return
	 */
	private IModule convertSoftList(URI databaseURI, URI zipUri, InputStream is) {
		IModule module;
		
		StreamXMLStorage storage = readXMLAndClose(is, "software", "softlist.xml");
		
		String moduleName = storage.getDocumentElement().getAttribute("name");
		module = new Module(databaseURI, moduleName);
		
		for (Element el : XMLUtils.getChildElements(storage.getDocumentElement())) {
			if (el.getNodeName().equals("description")) {
				moduleName = XMLUtils.getText(el);
				module.setName(moduleName);
			}
			else if (el.getNodeName().equals("year")
					|| el.getNodeName().equals("publisher")
					|| el.getNodeName().equals("info")) {
				//
			}
		}
		for (Element partEl : XMLUtils.getChildElementsNamed(storage.getDocumentElement(),  "part")) {
			if (!partEl.getAttribute("interface").equals("ti99_cart"))
				return null;
			
			//Element[] feats = XMLUtils.getChildElementsNamed(partEl, "feature");
			
			Element[] dataAreas = XMLUtils.getChildElementsNamed(partEl, "dataarea");
			for (Element dataArea : dataAreas) {
				String name = dataArea.getAttribute("name");
				if (name.equals("rom_socket")) {
					for (Element rom : XMLUtils.getChildElementsNamed(dataArea, "rom")) {
						injectModuleRom(module, databaseURI, moduleName, 
								makeZipUriString(zipUri, rom.getAttribute("name")),
								HexUtils.parseInt(rom.getAttribute("offset")),
								HexUtils.parseInt(rom.getAttribute("size"))
								);
						
					}
				}
				else if (name.equals("rom2_socket")) {
					for (Element rom : XMLUtils.getChildElementsNamed(dataArea, "rom")) {
						injectModuleBank2Rom(module, databaseURI, moduleName, 
								makeZipUriString(zipUri, rom.getAttribute("name")),
								HexUtils.parseInt(rom.getAttribute("offset")));
					}
				}
				else if (name.equals("grom_socket")) {
					for (Element rom : XMLUtils.getChildElementsNamed(dataArea, "rom")) {
						injectModuleGrom(module, databaseURI, moduleName, 
								makeZipUriString(zipUri, rom.getAttribute("name")),
								HexUtils.parseInt(rom.getAttribute("offset")));
					}
				}
			}
			
		}

		for (MemoryEntryInfo info : module.getMemoryEntryInfos())
			info.getProperties().put(MemoryEntryInfo.HAS_HEADER, true);

		return module;
	}

	/**
	 * @param is
	 * @return
	 */
	protected StreamXMLStorage readXMLAndClose(InputStream is, String rootTag, String fileName) {
		StreamXMLStorage storage = new StreamXMLStorage();
		storage.setInputStream(is);
		try {
			storage.load(rootTag);
			return storage;
		} catch (StorageException e) {
			log.error("failed to read <"+rootTag+"> from " + fileName, e);
			return null;
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * @param is
	 * @return
	 */
	private IModule convertLayoutMetainf(URI databaseURI, URI zipUri, InputStream lis,
			InputStream mis) {
		IModule module;

		StreamXMLStorage layout = readXMLAndClose(lis, "romset", "layout.xml");
		StreamXMLStorage metainf = mis != null ? readXMLAndClose(mis, "meta-inf", "meta-inf.xml") : null;
		
		if (layout == null) {
			log.error("problem parsing layout.xml or meta-inf.xml from " + zipUri);
			return null;
		}
		
		String moduleName = null;
		if (metainf != null) {
			Element moduleNameEl = XMLUtils.getChildElementNamed(metainf.getDocumentElement(), "name");
			if (moduleNameEl != null) {
				moduleName = moduleNameEl.getTextContent().trim();
			} else {
				log.debug("unexpected format, no <name>");
			}
		}
		if (moduleName == null) {
			// missing meta-inf, use filename as module name
			String path = zipUri.toString();
			int sidx = path.lastIndexOf('/');
			int qidx = path.lastIndexOf('?');
			moduleName = path.substring(Math.max(qidx, sidx) + 1);
		}
		module = new Module(databaseURI, moduleName);

		Element resources = XMLUtils.getChildElementNamed(layout.getDocumentElement(),  "resources");
		if (resources == null) {
			log.debug("unexpected format, no <resources>");
			return null;
		}

		Map<String, String> imgToFile = new HashMap<String, String>();
		for (Element romEl : XMLUtils.getChildElementsNamed(resources,  "rom")) {
			imgToFile.put(romEl.getAttribute("id"), romEl.getAttribute("file"));
		}
		
		Element config = XMLUtils.getChildElementNamed(layout.getDocumentElement(),  "configuration");
		if (config == null) {
			log.debug("unexpected format, no <configuration>");
			return null;
		}
		
		Element pcb = XMLUtils.getChildElementNamed(config,  "pcb");
		if (pcb == null) {
			log.debug("unexpected format, no <pcb>");
			return null;
		}
		
//		boolean banked = pcb.getAttribute("type").equals("banked");

		for (Element socketEl : XMLUtils.getChildElementsNamed(pcb,  "socket")) {
			String type = socketEl.getAttribute("id");
			String uri = makeZipUriString(zipUri, imgToFile.get(socketEl.getAttribute("uses")));

			if (type.equals("rom_socket")) {
				injectModuleRom(module, databaseURI, moduleName, uri,
						0, -0x2000
						);
			}
			else if (type.equals("rom2_socket")) {
				injectModuleBank2Rom(module, databaseURI, moduleName, uri, 
						0
						);
			}
			else if (type.equals("grom_socket")) {
				injectModuleGrom(module, databaseURI, moduleName, uri,
						0);
			}
			
		}

		for (MemoryEntryInfo info : module.getMemoryEntryInfos())
			info.getProperties().put(MemoryEntryInfo.HAS_HEADER, true);
		
		return module;
	}

	private URI makeZipUri(URI zipUri, String name) throws URISyntaxException {
		URI zipEntURI = new URI("jar", zipUri + "!/" + name, null);
		log.debug("Zip entry URI for " + zipUri + " is: " + zipEntURI);
		return zipEntURI;
	}

	private String makeZipUriString(URI zipUri, String name) {
		try {
			return makeZipUri(zipUri, name).toString();
		} catch (URISyntaxException e) {
			assert false;
			return name;
		}
	}
	
	private String readHeaderName(String name, ZipFile zipFile, String fname, String domain) {
		try {
			int idx = fname.lastIndexOf('/');
			if (idx > 0)
				fname = fname.substring(idx+1);
			ZipEntry entry = zipFile.getEntry(fname);
			if (entry == null)
				return null;
			InputStream is = zipFile.getInputStream(entry);
			byte[] content = FileUtils.readInputStreamContentsAndClose(is);
			String headerName = readHeaderName(name, content, 0x6000, domain);
			if (headerName.trim().equals("(auto-start)"))
				return name + " (auto-start)";
			else if (headerName.contains("auto-start"))
				return headerName;
			return name;
		} catch (IOException e) {
			return null;
		}
	}
		
	/**
	 * @param domain 
	 * @param file
	 * @return
	 */
	private String readHeaderName(String fname, byte[] content, int baseAddr, String domain) {
		// too large (even with accidental cruft)
		if (content.length > 0x10100)
			return null;
		
		for (int offs = 0; offs < content.length; offs += 0x2000) {
			int addr;
			
			log.debug("Module Header scan @ " + HexUtils.toHex4(offs) 
					+ ": header byte =  " + HexUtils.toHex2(content[offs]));

			// check non-module signs
			
			// ROMs
			addr = readAddr(content, offs);
			if (addr == 0x83e0)
				return null;	// console ROM
			
			
			if (content[offs] != (byte) 0xaa) {
				continue;
			}
			
			// powerup
			addr = readAddr(content, offs + 0x4);
			if (addr != 0 && addr >= 0x1000 && addr < 0x6000)
				return null;
			
			// DSR headers?
			addr = readAddr(content, offs + 0x8);
			if (addr != 0 && addr >= 0x4000 && addr < 0x6000)
				return null;
			
			addr = readAddr(content, offs + 0xA);
			if (addr != 0 && addr >= 0x1000 && addr < 0x6000)
				return null;
			
			// program list
			addr = readAddr(content, offs + 0x6);
			log.debug("Program list @ " + HexUtils.toHex4(offs) + ": " + HexUtils.toHex4(addr));
			if (addr == 0)
				continue;
			
			// not a module-area ROM
			if (addr < baseAddr)
				return null;
	
			// get the last name (ordered reverse, non-English first)
			String name;
			int next;
			do {
				next = readAddr(content, (addr - baseAddr));
				name = readString(content, (addr - baseAddr) + 4);
				log.debug("Fetched name: " + name) ;
				addr = next;
			} while (next >= baseAddr);		/* else not really module? */
			
			String suffix = "";
			if (content[offs + 1] < 0 && IMemoryDomain.NAME_GRAPHICS.equals(domain)) 
				suffix = " (auto-start)";
			if (isASCII(name)) {
				log.debug("Using name: " + name) ;
				
				return cleanupTitle(name) + suffix;
			}
			
			// see if it's auto-start
			if (suffix.length() > 0) {
				return new File(fname).getName() + suffix;
			}

		}
		
		return "";
	}

	/**
	 * @param name
	 * @return
	 */
	private boolean isASCII(String name) {
		for (char ch : name.toCharArray()) {
			if (ch < 0x20 || ch >= 127)
				return false;
		}
		return true;
	}

	/**
	 * @param addr
	 * @return
	 */
	private String readString(byte[] content, int addr) {
		if (addr < 0 || addr >= content.length)
			return "";
		int len = content[addr++] & 0xff;
		StringBuilder sb = new StringBuilder();
		while (len != 0 && addr >= 0 && addr < content.length) {
			sb.append((char) content[addr++]);
			len--;
		}
		return sb.toString();
	}

	/**
	 * @param content
	 * @param addr
	 * @return
	 */
	private int readAddr(byte[] content, int addr) {
		if (addr >= 0 && addr < content.length - 1)
			return ((content[addr] << 8) & 0xff00) | (content[addr+1] & 0xff);
		else
			return 0;
	}
	
	private String cleanupTitle(String allCaps) {
		allCaps = allCaps.trim();
		
		// remove spurious quotes
		allCaps = TextUtils.unquote(allCaps, '"');
		
		// capitalize each word
		StringBuilder sb = new StringBuilder();
		boolean newWord = true;
		for (char ch : allCaps.toCharArray()) {
			
			if (Character.isLetter(ch)) {
				if (newWord) {
					ch = Character.toUpperCase(ch);
					newWord = false;
				} else {
					ch = Character.toLowerCase(ch);
				}
			} else {
				newWord = true;
			}
				
			sb.append(ch);
		}
		
		String titledName = sb.toString();
		
		// lowercase prepositions
		for (String prep : new String[] { "And", "Or", "Of", "In", "For", "The" }) {
			titledName = replaceWord(titledName, prep, prep.toLowerCase(), false);
		}
				
		// uppercase common acronyms
		for (String acr : new String[] { "Ti", "Ii", "Iii" }) {
			titledName = replaceWord(titledName, acr, acr.toUpperCase(), true);
		}
				
		return titledName;		
	}

	private String replaceWord(String str, String word, String repl, boolean allowAtStart) {
		int idx = str.indexOf(word);
		if (idx > (allowAtStart ? -1 : 0)) {
			str = str.substring(0, idx) + repl + str.substring(idx + word.length());
		}

		return str;
	}
	
}
