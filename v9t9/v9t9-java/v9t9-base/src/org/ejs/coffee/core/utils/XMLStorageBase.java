package org.ejs.coffee.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements a simple persistent storage layer around XML.  Subclasses must specify
 * how the storage is read/written.
 * <p>
 * This class is not thread-safe.
 * @author eswartz
 *
 */
public abstract class XMLStorageBase {

	/** com.sun.org.apache.xml.internal.serializer#INDENT_AMOUNT */
	private static final String INDENT_AMOUNT = "{http://xml.apache.org/xalan}indent-amount";
	private boolean dirty;
	private Document document;
	private Element documentElement;
	
	public XMLStorageBase() {
		dirty = false;
		document = null;
		documentElement = null;
	}
	
	protected StorageException newStorageException(String message, Exception e) {
		return new StorageException(message, e);
	}
	/** 
	 * Get the stream for the existing content
	 * @return InputStream (will be closed by caller)
	 * @throws StorageException if storage does not exist or cannot be read
	 */
	abstract protected InputStream getStorageInputStream() throws StorageException;

	/** 
	 * Get the stream to write the new content
	 * @return OutputStream (will be closed by caller)
	 * @throws StorageException if storage cannot be created
	 */
	abstract protected OutputStream getStorageOutputStream() throws StorageException;
	
	/**
	 * Create an empty document.
	 * @throws StorageException if document could not be created (fatal!)
	 */
	public void create(String documentElementName) throws StorageException {
		document = null;
		documentElement = null;
		// not existing
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e2) {
			throw newStorageException("Failed to create XML document", e2);
		}
		
		documentElement = document.createElement(documentElementName);
		document.appendChild(documentElement);
		dirty = true;
	}
	
	/**
	 * Load the document.
	 * @throws StorageException if document could not be loade
	 */
	public void load(String documentElementName) throws StorageException {
		try {
			Document newDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
					getStorageInputStream());
			
			document = newDocument;
			documentElement = document.getDocumentElement();
			dirty = false;
		} catch (Exception e) {
			throw newStorageException("Failed to load XML storage", e);
		}
	}

	protected StorageException saveException(Exception e) {
		return newStorageException("Failed to persist XML storage", e);
	}

	/**
	 * Save the storage to XML.
	 * @throws StorageException
	 */
	public void save() throws StorageException {
		if (document == null)
			return;
		
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = factory.newTransformer();
		} catch (TransformerConfigurationException e) {
			throw saveException(e);
		}
		transformer.setOutputProperty(OutputKeys.STANDALONE, "yes"); //$NON-NLS-1$
		transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
		transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
		transformer.setOutputProperty(INDENT_AMOUNT, "2"); //$NON-NLS-1$
	
		OutputStream output = getStorageOutputStream();
		
		DOMSource source = new DOMSource(document);
		StreamResult outputTarget = new StreamResult(output);
		try {
			transformer.transform(source, outputTarget);
		} catch (TransformerException e) {
			throw saveException(e);
		}
		
		try {
			output.close();
			dirty = false;
		} catch (IOException e) {
			throw saveException(e);
		}
	}

	/**
	 * Tell whether the document has any changes.  This is set true by this
	 * class if the document was created anew due a failure to load from storage,
	 * or reset when the storage is loaded or saved successfully.
	 * @return true if dirty
	 */
	public boolean isDirty() {
		return dirty;
	}
	
	/**
	 * Set the dirty flag.
	 */
	public void setDirty(boolean anyChanges) {
		this.dirty = anyChanges;
	}

	/**
	 * 
	 */
	public Element getDocumentElement() {
		return documentElement;
	}
	
	/**
	 * Get the document
	 * @return
	 */
	public Document getDocument() {
		return document;
	}
}