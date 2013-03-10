/*
  XMLUtils.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.utils;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Utility functions for dealing with XML.
 * @author eswartz
 *
 */
public class XMLUtils {

	/**
	 * Get the Element children of an element as an array.
	 * @param element
	 * @return non-<code>null</code> array 
	 */
	public static Element[] getChildElements(Element element) {
		NodeList childNodes = element.getChildNodes();
		List<Element> kids = new ArrayList<Element>(); 
		Node node = childNodes.item(0);
		while (node != null) {
			if (node instanceof Element) {
				kids.add((Element) node);
			}
			node = node.getNextSibling();
		}
		return (Element[]) kids.toArray(new Element[kids.size()]);
	}
	
	/**
	 * Get the elements with the given name.
	 * @param element
	 * @param name
	 * @return array, never <code>null</code>
	 */
	public static Element[] getChildElementsNamed(Element element, String name) {
		if (element == null)
			return new Element[0];
		NodeList nodeList = element.getChildNodes();
		List<Element> elements = new ArrayList<Element>(); 
		Node node = nodeList.item(0);
		while (node != null) {
			if (node instanceof Element && name.equals(node.getNodeName())) {
				elements.add((Element) node);
			}
			node = node.getNextSibling();
		}
		return (Element[]) elements.toArray(new Element[elements.size()]);
	}

	/**
	 * Find or create a child element with the given name
	 * @param element
	 * @param name
	 * @return new or existing element
	 */
	public static Element findOrCreateChildElement(Document document, Element element, String name) {
		Element[] elements = getChildElementsNamed(element, name);
		Element kid; 
		if (elements.length > 0) {
			kid = elements[0];
		} else {
			kid = document.createElement(name);
			element.appendChild(kid);
		}
		return kid;
	}

	/**
	 * Set or remove text for an element.
	 * @param element
	 * @param text the text to set or <code>null</code> to remove
	 */
	public static void setText(Element element, String text) {
		element.setTextContent(text);
	}

	/**
	 * Get the text for an element, which is considered the first Text child element.
	 * @param element
	 * @returns the text or <code>null</code>
	 */
	public static String getText(Element element) {
		NodeList nodeList = element.getChildNodes();
		if (nodeList.item(0) instanceof Text) {
			return nodeList.item(0).getTextContent();
		}
		return null;
	}
	
	/**
	 * Get the trimmed text for an element, which is considered the first Text child element.
	 * @param element
	 * @returns the text or <code>""</code>
	 */
	public static String getTrimmedText(Element element) {
		String text = getText(element);
		if (text != null)
			return text.trim();
		return null;
	}

}
