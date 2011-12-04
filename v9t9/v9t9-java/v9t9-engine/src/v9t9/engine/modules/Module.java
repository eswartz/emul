/**
 * 
 */
package v9t9.engine.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import v9t9.base.utils.HexUtils;
import v9t9.base.utils.XMLUtils;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.modules.IModule;
import v9t9.common.modules.MemoryEntryInfo;
import v9t9.engine.memory.BankedMemoryEntry;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.StdMultiBankedMemoryEntry;

/**
 * @author ejs
 *
 */
public class Module implements IModule {

	private List<MemoryEntryInfo> entries = new ArrayList<MemoryEntryInfo>();
	private String name;
	
	public Module(String name) {
		this.name = name;
	}
	public Module(String name, List<MemoryEntryInfo> entries) {
		this.name = name;
		this.entries = entries;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.modules.IModule#getEntries()
	 */
	public MemoryEntryInfo[] getMemoryEntryInfos() {
		return (MemoryEntryInfo[]) entries.toArray(new MemoryEntryInfo[entries.size()]);
	}
	/* (non-Javadoc)
	 * @see v9t9.engine.modules.IModule#getName()
	 */
	public String getName() {
		return name;
	}
	
	public void loadFrom(Element module) {
		String name = module.getAttribute("name");
		this.name = name;
		
		Element[] entries = XMLUtils.getChildElementsNamed(module, "moduleEntries");
		for (Element entry : entries) {
			for (Element el : XMLUtils.getChildElements(entry)) {
				MemoryEntryInfo info = new MemoryEntryInfo();
				Map<String, Object> properties = info.getProperties();
				this.entries.add(info);
				
				properties.put(MemoryEntryInfo.NAME, name);
				
				// helpers
				if (el.getNodeName().equals("romModuleEntry")) {
					properties.put(MemoryEntryInfo.DOMAIN, IMemoryDomain.NAME_CPU);
					properties.put(MemoryEntryInfo.ADDRESS, 0x6000);
					properties.put(MemoryEntryInfo.SIZE, 0x2000);
				}
				if (el.getNodeName().equals("gromModuleEntry")) {
					properties.put(MemoryEntryInfo.DOMAIN, IMemoryDomain.NAME_GRAPHICS);
					properties.put(MemoryEntryInfo.ADDRESS, 0x6000);
					properties.put(MemoryEntryInfo.SIZE, 0x0);
				}
				if (el.getNodeName().equals("bankedModuleEntry")) {
					properties.put(MemoryEntryInfo.DOMAIN, IMemoryDomain.NAME_CPU);
					properties.put(MemoryEntryInfo.ADDRESS, 0x6000);
					
					if ("true".equals(el.getAttribute("custom"))) {
						properties.put(MemoryEntryInfo.CLASS, BankedMemoryEntry.class);
					} else {
						properties.put(MemoryEntryInfo.CLASS, StdMultiBankedMemoryEntry.class);
					}
				}
				
				
				getStringAttribute(el, MemoryEntryInfo.FILENAME, info);
				getStringAttribute(el, MemoryEntryInfo.FILENAME2, info);
				getStringAttribute(el, MemoryEntryInfo.DOMAIN, info);
				getIntAttribute(el, MemoryEntryInfo.ADDRESS, info);
				getIntAttribute(el, MemoryEntryInfo.SIZE, info);
				getIntAttribute(el, MemoryEntryInfo.OFFSET, info);
				getBooleanAttribute(el, MemoryEntryInfo.STORED, info);
				getClassAttribute(el, MemoryEntryInfo.CLASS, MemoryEntry.class, info);
			}
		}
	}
	/**
	 * @param el
	 * @param class1
	 * @param info
	 */
	private void getClassAttribute(Element el, String name, Class<?> baseKlass,
			MemoryEntryInfo info) {

		if (el.hasAttribute(name)) {
			Class<?> klass;
			
			String klassName = el.getAttribute(name);
			try {
				klass = getClass().getClassLoader().loadClass(klassName);
				if (!baseKlass.isAssignableFrom(klass)) {
					System.err.println("Illegal class: wanted instance of " + baseKlass + " but got " + klass);
				} else {
					info.getProperties().put(name, klass);
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	public void getStringAttribute(Element el, String name, MemoryEntryInfo info) {
		if (el.hasAttribute(name)) {
			String attr = el.getAttribute(name);
			info.getProperties().put(name, attr);
		}
	}
	public void getIntAttribute(Element el, String name, MemoryEntryInfo info) {
		if (el.hasAttribute(name)) {
			String attr = el.getAttribute(name);
			info.getProperties().put(name, HexUtils.parseInt(attr));
		}
	}
	public void getBooleanAttribute(Element el, String name, MemoryEntryInfo info) {
		if (el.hasAttribute(name)) {
			String attr = el.getAttribute(name);
			info.getProperties().put(name, "true".equals(attr));
		}
	}
	
}
