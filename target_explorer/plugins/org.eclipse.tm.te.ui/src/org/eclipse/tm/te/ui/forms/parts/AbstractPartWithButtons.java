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

import java.util.Arrays;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Target Explorer: Abstract part with buttons implementation.
 */
public abstract class AbstractPartWithButtons extends AbstractPart {
	// The button labels
	private final String[] labels;
	// The buttons list
	private Button[] buttons = null;

	/**
	 * Constructor.
	 *
	 * @param labels The list of label to apply to the created buttons in the given order. Must not be <code>null</code>.
	 */
	public AbstractPartWithButtons(String[] labels) {
		super();
		Assert.isNotNull(labels);
		this.labels = labels;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.forms.parts.AbstractPart#createControl(org.eclipse.swt.widgets.Composite, int, int, org.eclipse.ui.forms.widgets.FormToolkit)
	 */
	@Override
	public void createControl(Composite parent, int style, int span, FormToolkit toolkit) {
		Assert.isNotNull(parent);
		createMainLabel(parent, span, toolkit);
		createMainControl(parent, style, span - 1, toolkit);
		createButtonsPanel(parent, toolkit);
	}

	/**
	 * Creates the part main control(s).
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @param style The control style if applicable.
	 * @param span The horizontal span if applicable.
	 * @param toolkit The form toolkit or <code>null</code>.
	 */
	protected abstract void createMainControl(Composite parent, int style, int span, FormToolkit toolkit);

	/**
	 * Create the part main label control.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @param span The horizontal span if applicable.
	 * @param toolkit The form toolkit or <code>null</code>.
	 */
	protected void createMainLabel(Composite parent, int span, FormToolkit toolkit) {
		Assert.isNotNull(parent);
	}

	/**
	 * Create the buttons panel.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @param toolkit The form toolkit or <code>null</code>.
	 *
	 * @return The buttons panel composite or <code>null</code>.
	 */
	protected Composite createButtonsPanel(Composite parent, FormToolkit toolkit) {
		if (labels.length == 0) return null;

		buttons = new Button[labels.length];

		Composite panel = createComposite(parent, toolkit);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0; layout.marginWidth = 0;
		panel.setLayout(layout);
		GridData layoutData = new GridData(SWT.BEGINNING, SWT.FILL, false, true);
		panel.setLayoutData(layoutData);

		SelectionListener listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AbstractPartWithButtons.this.onButtonSelected((Button) e.widget);
			}
		};

		for (int i = 0; i < labels.length; i++) {
			if (labels[i] != null) {
				Button button = toolkit != null ? toolkit.createButton(panel, null, SWT.PUSH) : new Button(panel, SWT.PUSH);
				Assert.isNotNull(button);

				button.setFont(JFaceResources.getDialogFont());
				button.setText(labels[i]);
				button.setData(Integer.valueOf(i));
				button.addSelectionListener(listener);

				layoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
				layoutData.widthHint = Math.max(new PixelConverter(button).convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH), button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
				button.setLayoutData(layoutData);

				buttons[i] = button;
			} else {
				createEmptySpace(parent, 1, toolkit);
			}
		}

		return panel;
	}

	/**
	 * Called from the buttons selection listener to signal when
	 * the user clicked on the button.
	 *
	 * @param button The button selected. Must not be <code>null</code>
	 */
	protected void onButtonSelected(Button button) {
		Assert.isNotNull(button);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.forms.parts.AbstractPart#onEnabledStateChanged()
	 */
	@Override
	protected void onEnabledStateChanged() {
		for (Button button : buttons) {
			if (button != null && !button.isDisposed()) {
				button.setEnabled(isEnabled());
			}
		}
	}

	/**
	 * Returns the button with the given label.
	 *
	 * @param label The button label.
	 * @return The button.
	 *
	 * @throws ArrayIndexOutOfBoundsException if the label is invalid.
	 */
	public Button getButton(String label) {
		return getButton(Arrays.asList(labels).indexOf(label));
	}

	/**
	 * Returns the button at the given index.
	 *
	 * @param index The index.
	 * @return The button.
	 *
	 * @throws ArrayIndexOutOfBoundsException if the index is out of bounds.
	 */
	public Button getButton(int index) {
		return buttons[index];
	}
}
