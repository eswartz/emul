/*
  IEmulatorContentHandler.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.client;

import org.eclipse.jface.resource.ImageDescriptor;

import v9t9.common.events.NotifyException;

/**
 * This interface  allows handling content in the
 * UI, when there is more than one choice.
 * @author ejs
 *
 */
public interface IEmulatorContentHandler {
	IEmulatorContentHandler[] NONE = new IEmulatorContentHandler[0];

	/**
	 * Get the human-readable label
	 * @return
	 */
	String getLabel();
	
	/**
	 * Get the optional image for the handler
	 * @return
	 */
	ImageDescriptor getImage();
	
	/**
	 * Describe how the content will be handled
	 * @param source 
	 */
	String getDescription();
	
	/**
	 * Handle the content
	 * @param source the source
	 * @throws NotifyException if the source could not be correctly handled
	 */
	void handleContent() throws NotifyException;

	/**
	 * Tell if the handler requires that the user willingly
	 * accept it (this blocks automatic handling)
	 * @return
	 */
	boolean requireConfirmation();
}
