/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.controls.validator;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.tm.te.ui.controls.nls.Messages;


/**
 * Input validator and message provider.
 */
public abstract class Validator implements IMessageProvider, ICellEditorValidator {

	// message text, set in isValid(String)
	private String message = null;
	// message type, set in isValid(String)
	private int messageType = NONE;
	// map with all message texts
	private Map<String, String> messages = new HashMap<String, String>();
	// map with all message text types
	private Map<String, Integer> messageTypes = new HashMap<String, Integer>();

	// arguments (binary coded)
	public static final int NO_ATTR = 0;
	public static final int ATTR_MANDATORY = 1;
	// next attribute should start with 2^1

	// binary coded arguments
	private int attributes;

	/**
	 * Constructor
	 * @param attributes The validator attributes.
	 */
	public Validator(int attributes) {
		setAttributes(attributes);
	}

	/**
	 * Set the attributes for the validator.
	 * @param attributes The validator attributes.
	 */
	public void setAttributes(int attributes) {
		this.attributes = attributes;
	}

	/**
	 * Add an attribute.
	 * @param attribute The validator attribute to add.
	 */
	public void addAttribute(int attribute) {
		if (!isAttribute(attribute)) {
			this.attributes |= attribute;
		}
	}

	/**
	 * Remove an attribute.
	 * @param attribute The validator attribute to remove.
	 */
	public void delAttribute(int attribute) {
		if (isAttribute(attribute)) {
			this.attributes -= attribute;
		}
	}

	/**
	 * Returns the attributes.
	 * @return
	 */
	public int getAttributes() {
		return attributes;
	}

	/**
	 * Returns true if the argument is set.
	 * @param attribute The argument to ask for.
	 * @return
	 */
	public boolean isAttribute(int attribute) {
		return isAttribute(attribute, attributes);
	}

	/**
	 * Returns true is argument is set.
	 * This static method can be used in the constructor or other static methods
	 * to check attributes.
	 * @param attribute The attribute to ask for
	 * @param attributes The binary coded attribute list
	 * @return
	 */
	public static boolean isAttribute(int attribute, int attributes) {
		return ((attributes & attribute) == attribute);
	}

	/**
	 * Initialize the validator.
	 * Should always be called in isValid(String) before validation is done.
	 * Doesn't reset message texts!
	 */
	protected void init() {
		setMessage(null);
		setMessageType(NONE);
	}

	/**
	 * Validates the given text.
	 * A message text could be set even when the text is valid!
	 * @param newText text to validate
	 * @return true if text is valid
	 */
	public abstract boolean isValid(String newText);

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICellEditorValidator#isValid(java.lang.Object)
	 */
	@Override
	public final String isValid(Object newValue) {
		String strValue = (newValue != null) ? newValue.toString() : null;
		if (!isValid(strValue)) {
			return (getMessage());
		}
		return null;
	}

	/**
	 * Sets the message text and type.
	 * @param message message
	 * @param messageType type for message
	 */
	protected final void setMessage(String message, int messageType) {
		setMessage(message);
		setMessageType(messageType);
	}

	/**
	 * Sets the message.
	 * @param message message
	 */
	protected final void setMessage(String message) {
		this.message = message;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IMessageProvider#getMessage()
	 */
	@Override
	public final String getMessage() {
		return message;
	}

	/**
	 * Sets the message type.
	 * @param messageType type for message
	 */
	protected final void setMessageType(int messageType) {
		this.messageType = messageType;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IMessageProvider#getMessageType()
	 */
	@Override
	public final int getMessageType() {
		return messageType;
	}

	/**
	 * Returns the message text for the given key.
	 * The key always points to the default text.
	 * @param key message key
	 * @return message text
	 */
	protected final String getMessageText(String key) {
		String message = messages.get(key);
		if (message == null) {
			message = getString(key);
			setMessageText(key, message);
		}
		return message;
	}

	/**
	 * Returns the message type for the given key. If not set, the
	 * proposed message type is returned.
	 *
	 * @param key The message key.
	 * @param proposedType The proposed message type.
	 *
	 * @return The message type.
	 */
	protected final int getMessageTextType(String key, int proposedType) {
		Integer type = messageTypes.get(key);
		if (type == null || type.intValue() == -1) type = Integer.valueOf(proposedType);
		return type.intValue();
	}

	/**
	 * Sets an alternate message text for an info, warning or error on isValid(String).
	 *
	 * @param key property key of default text
	 * @param text alternate message text, if null the default text for this key is taken
	 */
	public final void setMessageText(String key, String text) {
		setMessageText(key, text, -1);
	}

	/**
	 * Sets an alternate message text for an info, warning or error on isValid(String).
	 *
	 * @param key property key of default text
	 * @param text alternate message text, if null the default text for this key is taken
	 * @param type alternate message type, if -1 the default type for this key is taken
	 */
	public final void setMessageText(String key, String text, int type) {
		if (key != null) {
			if (text != null) {
				this.messages.put(key, text);
			} else {
				this.messages.put(key, getString(key));
			}
			if (type != -1) {
				this.messageTypes.put(key, Integer.valueOf(type));
			} else {
				this.messageTypes.remove(key);
			}
		}
	}

	/**
	 * Returns the externalized string value for the given key.
	 *
	 * @param key The key. Must not be <code>null</code>.
	 * @return The string value or <code>null</code>
	 */
	protected String getString(String key) {
		Assert.isNotNull(key);
		return Messages.getString(key);
	}
}
