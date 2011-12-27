/**
 * 
 */
package v9t9.engine.modules;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import ejs.base.utils.HexUtils;
import ejs.base.utils.XMLUtils;

import v9t9.common.machine.IMachine;
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
	private String image;
	private URL imageURL;
	
	public Module(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		return "Module: " + name;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.modules.IModule#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModule#getImageURL()
	 */
	@Override
	public String getImagePath() {
		return image;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModule#getImageURL()
	 */
	@Override
	public URL getImageURL() {
		return imageURL;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.modules.IModule#getEntries()
	 */
	public MemoryEntryInfo[] getMemoryEntryInfos() {
		return (MemoryEntryInfo[]) entries.toArray(new MemoryEntryInfo[entries.size()]);
	}
	
	public void loadFrom(IMachine machine, Element module) {
		String name = module.getAttribute("name");
		this.name = name;

		Element[] entries;
		
		entries = XMLUtils.getChildElementsNamed(module, "image");
		for (Element el : entries) {
			image = el.getTextContent().trim();
			try {
				URL url = new URL(machine.getModel().getDataURL(), image);
				url.openStream().close();
				imageURL = url;
				break;
			} catch (MalformedURLException e) {
			} catch (IOException e) {
			}
		}
		
		entries = XMLUtils.getChildElementsNamed(module, "moduleEntries");
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
				else if (el.getNodeName().equals("gromModuleEntry")) {
					properties.put(MemoryEntryInfo.DOMAIN, IMemoryDomain.NAME_GRAPHICS);
					properties.put(MemoryEntryInfo.ADDRESS, 0x6000);
					properties.put(MemoryEntryInfo.SIZE, 0x0);
				}
				else if (el.getNodeName().equals("bankedModuleEntry")) {
					properties.put(MemoryEntryInfo.DOMAIN, IMemoryDomain.NAME_CPU);
					properties.put(MemoryEntryInfo.ADDRESS, 0x6000);
					
					if ("true".equals(el.getAttribute("custom"))) {
						properties.put(MemoryEntryInfo.CLASS, BankedMemoryEntry.class);
					} else {
						properties.put(MemoryEntryInfo.CLASS, StdMultiBankedMemoryEntry.class);
					}
				}
				else if (!el.getNodeName().equals("moduleEntry")) {
					System.err.println("Unknown entry: " + el.getNodeName());
					continue;
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
