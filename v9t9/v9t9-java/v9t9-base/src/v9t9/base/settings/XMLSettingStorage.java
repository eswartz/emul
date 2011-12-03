/**
 * 
 */
package v9t9.base.settings;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import v9t9.base.settings.ISettingSection.Type;
import v9t9.base.utils.StorageException;
import v9t9.base.utils.StreamXMLStorage;
import v9t9.base.utils.XMLUtils;

/**
 * @author ejs
 *
 */
public class XMLSettingStorage implements ISettingStorage {

	/**
	 * 
	 */
	private static final String SECTION = "section";
	private ISettingStorageObjectHandler objectHandler;
	private final String rootElement;

	/**
	 * 
	 */
	public XMLSettingStorage(String rootElement) {
		this.rootElement = rootElement;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.base.core.settings.ISettingStorage#setObjectHandler(v9t9.base.core.settings.ISettingStorageObjectHandler)
	 */
	public void setObjectHandler(ISettingStorageObjectHandler handler) {
		this.objectHandler = handler;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.base.core.settings.ISettingStorage#load(java.io.File)
	 */
	public ISettingSection load(InputStream inputStream) throws IOException {
		StreamXMLStorage storage = new StreamXMLStorage();
		storage.setInputStream(inputStream);
		ISettingSection root = new SettingsSection();
		try {
			storage.load(rootElement);

			loadSection(root, storage.getDocumentElement());
			
		} catch (StorageException e) {
			if (e.getCause() instanceof IOException)
				throw (IOException) e.getCause();
			throw (IOException) new IOException().initCause(e);
		}
		return root;
	}

	/**
	 * @param section 
	 * @param documentElement
	 */
	private void loadSection(ISettingSection section, Element sectionElement) {
		
		NodeList nodeList = sectionElement.getChildNodes();
		Node node = nodeList.item(0);
		while (node != null) {
			if (node instanceof Element) {
				Element element = (Element) node;
				node = node.getNextSibling();
				String type = element.getNodeName();
				String valueStr = XMLUtils.getTrimmedText(element);
				String name = element.getAttribute("name");
				if (name.length() == 0) {
					if (type.equals("item")) {
						// compatibility
						loadDialogSettingsSection(section, sectionElement);
						return;
					}
					continue;
				}
				
				Object value = valueStr; 
				if (type.equals(Type.Int.getXmlName())) {
					try {
						value = Integer.parseInt(valueStr);
					} catch (NumberFormatException e) {
					}
				} else if (type.equals(Type.Double.getXmlName())) {
					try {
						value = Double.parseDouble(valueStr);
					} catch (NumberFormatException e) {
					}
				} else if (type.equals(Type.Boolean.getXmlName())) {
					value = Boolean.getBoolean(valueStr);
				} else if (type.equals(Type.Object.getXmlName())) {
					if (objectHandler != null)
						value = objectHandler.decodeObject(valueStr);
					else
						value = valueStr;
				} else if (type.equals(Type.String.getXmlName())) {
					// done
				} else if (type.equals(Type.StringArray.getXmlName())) {
					Element[] stringElements = XMLUtils.getChildElementsNamed(element, Type.String.getXmlName());
					String[] strings = new String[stringElements.length];
					for (int i = 0; i < strings.length; i++) {
						strings[i] = XMLUtils.getTrimmedText(stringElements[i]).trim();
					}
					value = strings;
				} else if (type.equals(Type.Section.getXmlName())) {
					ISettingSection subSection = new SettingsSection();
					loadSection(subSection, element);
					value = subSection;
				} else {
					continue;
				}
				
				section.put(name, value);
			} else {
				node = node.getNextSibling();
			}
		}
	}

	/**
	 * @param section
	 * @param sectionElement
	 */
	private void loadDialogSettingsSection(ISettingSection section,
			Element sectionElement) {
		for (Element itemElement : XMLUtils.getChildElementsNamed(sectionElement, "item")) {
			String value = itemElement.getAttribute("value");
			String name = itemElement.getAttribute("key");
			if (name.length() == 0)
				continue;
			
			section.put(name, value);
		}
		
		for (Element listElement : XMLUtils.getChildElementsNamed(sectionElement, "list")) {
			String name = listElement.getAttribute("key");
			if (name.length() == 0)
				continue;
			
			Element[] stringElements = XMLUtils.getChildElementsNamed(listElement, "item");
			String[] strings = new String[stringElements.length];
			for (int i = 0; i < strings.length; i++) {
				strings[i] = stringElements[i].getAttribute("value");
			}
			section.put(name, strings);
		}
		
		for (Element subSectionElement : XMLUtils.getChildElementsNamed(sectionElement, SECTION)) {
			String name = subSectionElement.getAttribute("name");
			if (name.length() == 0)
				continue;
			
			ISettingSection subSection = new SettingsSection();
			loadDialogSettingsSection(subSection, subSectionElement);
			
			section.put(name, subSection);
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.base.core.settings.ISettingStorage#save(java.io.File)
	 */
	public void save(OutputStream outputStream, ISettingSection section) throws IOException {
		StreamXMLStorage storage = new StreamXMLStorage();
		storage.setOutputStream(outputStream);
		try {
			storage.create(rootElement);
			saveSection(storage.getDocumentElement(), section);
			storage.save();
		} catch (StorageException e) {
			if (e.getCause() instanceof IOException)
				throw (IOException) e.getCause();
			throw (IOException) new IOException().initCause(e);
		}
	}
	
	private void saveSection(Element sectionElement, ISettingSection section) {
		if (section == null)
			return;

		// normal settings first
		Document doc = sectionElement.getOwnerDocument();
		for (ISettingSection.SettingEntry entry : section) {
			Element el = doc.createElement(entry.type.getXmlName());
			
			el.setAttribute("name", entry.name);
			if (entry.type == Type.Section) {
				continue;
			} else if (entry.type == Type.StringArray) {
				for (String str : (String[]) entry.value) {
					Element strel = el.getOwnerDocument().createElement(Type.String.getXmlName());
					el.appendChild(strel);
					XMLUtils.setText(strel, str);
				}
			} else {
				XMLUtils.setText(el, entry.value.toString());
			}
			sectionElement.appendChild(el);
		}
		
		// sections at end
		for (ISettingSection.SettingEntry entry : section) {
			if (entry.type == Type.Section) {
				Element el = doc.createElement(entry.type.getXmlName());
				el.setAttribute("name", entry.name);
				saveSection(el, (ISettingSection) entry.value);
				sectionElement.appendChild(el);
			}
		}
	}

}
