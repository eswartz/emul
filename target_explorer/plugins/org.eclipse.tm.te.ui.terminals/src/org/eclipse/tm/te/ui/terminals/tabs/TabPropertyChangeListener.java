/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.terminals.tabs;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Font;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.view.TerminalView;

/**
 * Terminals tab default property change listener implementation.
 */
@SuppressWarnings("restriction")
public class TabPropertyChangeListener implements IPropertyChangeListener {
	/**
	 * Default terminal font property key.
	 *
	 * @see TerminalView#FONT_DEFINITION
	 */
	public static final String FONT_DEFINITION = "terminal.views.view.font.definition"; //$NON-NLS-1$

	// Reference to the parent tab item
	private final CTabItem tabItem;

	/**
	 * Constructor.
	 *
	 * @param tabItem The parent tab item. Must not be <code>null</code>.
	 */
	public TabPropertyChangeListener(CTabItem tabItem) {
		super();
		Assert.isNotNull(tabItem);
		this.tabItem = tabItem;
	}

	/**
	 * Returns the associated parent tab item.
	 *
	 * @return The parent tab item.
	 */
	protected final CTabItem getTabItem() {
		return tabItem;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		// In case we are called once after the tab item got disposed
		// --> Do nothing
		if (getTabItem() == null || getTabItem().isDisposed()) return;

		// Listen to changes of the Font settings
		if (event.getProperty().equals(FONT_DEFINITION)) {
			onFontDefinitionProperyChanged();
		}
	}

	/**
	 * Called if a property change event for the terminal font
	 * definition is received.
	 */
	protected void onFontDefinitionProperyChanged() {
		// Get the current font from JFace
		Font font = JFaceResources.getFont(FONT_DEFINITION);
		// Get the terminal control from the tab item
		if (getTabItem().getData() instanceof ITerminalViewControl) {
			ITerminalViewControl terminal = (ITerminalViewControl)getTabItem().getData();
			terminal.setFont(font);
		}
	}
}
