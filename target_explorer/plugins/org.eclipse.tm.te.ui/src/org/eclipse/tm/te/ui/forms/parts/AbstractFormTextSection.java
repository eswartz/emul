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
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm.te.ui.forms.FormLayoutFactory;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;

/**
 * Target Explorer: Abstract form text section implementation.
 */
public abstract class AbstractFormTextSection extends AbstractSection implements IHyperlinkListener {

	/**
	 * Constructor.
	 *
	 * @param form The parent managed form. Must not be <code>null</code>.
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @param style The section style.
	 */
	public AbstractFormTextSection(IManagedForm form, Composite parent, int style) {
		this(form, parent, style, true);
	}

	/**
	 * Constructor.
	 *
	 * @param form The parent managed form. Must not be <code>null</code>.
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @param style The section style.
	 * @param titleBar If <code>true</code>, the title bar style bit is added to <code>style</code>.
	 */
	public AbstractFormTextSection(IManagedForm form, Composite parent, int style, boolean titleBar) {
		super(form, parent, style, titleBar);
		getSection().setLayout(FormLayoutFactory.createClearTableWrapLayout(false, 1));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.forms.parts.AbstractSection#createClient(org.eclipse.ui.forms.widgets.Section, org.eclipse.ui.forms.widgets.FormToolkit)
	 */
	@Override
	protected void createClient(Section section, FormToolkit toolkit) {
		Assert.isNotNull(section);
		Assert.isNotNull(toolkit);

		// Configure the section
		section.setText(getSectionTitle());

		section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Create the section client
		Composite client = toolkit.createComposite(section, SWT.NONE);
		Assert.isNotNull(client);
		client.setLayout(FormLayoutFactory.createSectionClientTableWrapLayout(false, 1));
		client.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		FormText text = createFormText(client, toolkit);
		configureFormText(text);

		section.setClient(client);
	}

	/**
	 * Returns the section title.
	 *
	 * @return The section title.
	 */
	protected abstract String getSectionTitle();

	/**
	 * Returns the form text content.
	 *
	 * @return The form text content.
	 */
	protected abstract String getFormTextContent();

	/**
	 * Creates a form text control in the given parent composite with the given
	 * content and form toolkit.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @param toolkit The toolkit. Must not be <code>null</code>.
	 *
	 * @return The form text control.
	 */
	protected final FormText createFormText(Composite parent, FormToolkit toolkit) {
		Assert.isNotNull(parent);
		Assert.isNotNull(toolkit);

		// Create the form text control
		return toolkit.createFormText(parent, true);
	}

	/**
	 * Configure the form text.
	 * <p>
	 * The default implementation is setting the form text content
	 * and adding ourself as hyper link listener. The form text
	 * content is queried through {@link #getFormTextContent()}.
	 *
	 * @param text The form text. Must not be <code>null</code>.
	 */
	protected void configureFormText(FormText text) {
		Assert.isNotNull(text);

		// Set the content. If it fails, set the failure message
		try {
			text.setText(getFormTextContent(), true, false);
		} catch (SWTException e) {
			text.setText(e.getMessage(), false, false);
		}

		// Add ourself as hyper link listener
		text.addHyperlinkListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.events.IHyperlinkListener#linkEntered(org.eclipse.ui.forms.events.HyperlinkEvent)
	 */
	public void linkEntered(HyperlinkEvent e) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.events.IHyperlinkListener#linkExited(org.eclipse.ui.forms.events.HyperlinkEvent)
	 */
	public void linkExited(HyperlinkEvent e) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.events.IHyperlinkListener#linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent)
	 */
	public void linkActivated(HyperlinkEvent e) {
	}
}
