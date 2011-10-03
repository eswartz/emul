/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.forms.parts;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Target Explorer: Abstract structured viewer section implementation.
 */
public abstract class AbstractStructuredViewerSection extends AbstractSection {
	// The structured viewer part instance
	private AbstractStructuredViewerPart viewerPart;

	/**
	 * Constructor.
	 *
	 * @param form The parent managed form. Must not be <code>null</code>.
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @param style The section style.
	 * @param labels The list of label to apply to the created buttons in the given order. Must not be <code>null</code>.
	 */
	public AbstractStructuredViewerSection(IManagedForm form, Composite parent, int style, String[] labels) {
		this(form, parent, style, true, labels);
	}

	/**
	 * Constructor.
	 *
	 * @param form The parent managed form. Must not be <code>null</code>.
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @param style The section style.
	 * @param titleBar If <code>true</code>, the title bar style bit is added to <code>style</code>.
	 * @param labels The list of label to apply to the created buttons in the given order. Must not be <code>null</code>.
	 */
	public AbstractStructuredViewerSection(IManagedForm form, Composite parent, int style, boolean titleBar, String[] labels) {
		super(form, parent, style, titleBar);

		viewerPart = createViewerPart(labels);
		viewerPart.setMinSize(50, 50);

		createClient(getSection(), form.getToolkit());
	}

	/**
	 * Creates the structured viewer part instance.
	 *
	 * @param labels The list of label to apply to the created buttons in the given order. Must not be <code>null</code>.
	 * @return The viewer part instance.
	 */
	protected abstract AbstractStructuredViewerPart createViewerPart(String[] labels);

	/**
	 * Sets the structured viewer part instance.
	 *
	 * @param viewerPart The viewer part instance or <code>null</code>.
	 */
	protected void setViewerPart(AbstractStructuredViewerPart viewerPart) {
		this.viewerPart = viewerPart;
	}

	/**
	 * Returns the structured viewer part instance.
	 *
	 * @return The viewer part instance of <code>null</code>.
	 */
	protected final AbstractStructuredViewerPart getViewerPart() {
		return viewerPart;
	}

	/**
	 * Invoke the viewer part instance control creation..
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @param style The control style.
	 * @param span The horizontal span.
	 * @param toolkit The form toolkit or <code>null</code>.
	 */
	protected void createPartControl(Composite parent, int style, int span, FormToolkit toolkit) {
		Assert.isNotNull(parent);
		Assert.isNotNull(viewerPart);

		// Create the viewer part controls
		viewerPart.createControl(parent, style, span, toolkit);
		Assert.isNotNull(viewerPart.getViewer());
		Assert.isNotNull(viewerPart.getViewer().getControl());

		// Create and initialize the menu manager
		MenuManager manager = new MenuManager();
		manager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager2) {
				fillContextMenu(manager2);
			}
		});
		manager.setRemoveAllWhenShown(true);

		Control control = viewerPart.getViewer().getControl();
		control.setMenu(manager.createContextMenu(control));

		registerContextMenu(manager);
	}

	/**
	 * Register the context menu to a view site if needed.
	 * <p>
	 * The default implementation does nothing.
	 *
	 * @param manager The context menu manager. Must not be <code>null</code>.
	 */
	protected void registerContextMenu(MenuManager manager) {
	}

	/**
	 * Called by the context menu menu listener if the menu
	 * is about to show.
	 * <p>
	 * The default implementation does nothing.
	 *
	 * @param manager The menu manager. Must not be <code>null</code>.
	 */
	protected void fillContextMenu(IMenuManager manager) {
	}

	/**
	 * Called from the viewer parts buttons selection listener
	 * to signal when the user clicked on the button.
	 *
	 * @param button The button selected. Must not be <code>null</code>
	 */
	protected void onButtonSelected(Button button) {
		Assert.isNotNull(button);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.SectionPart#setFocus()
	 */
	@Override
	public void setFocus() {
		if (viewerPart != null) viewerPart.getViewer().getControl().setFocus();
	}
}
