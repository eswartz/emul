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
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tm.te.ui.swt.SWTControlUtil;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Abstract part implementation.
 */
public abstract class AbstractPart extends PlatformObject {
	// The parts enabled state
	private boolean enabled = true;

	/**
	 * Constructor.
	 */
	public AbstractPart() {
		super();
	}

	/**
	 * Sets the parts enabled state and call {@link #onEnabledStateChanged()}
	 * if the enabled state changed.
	 *
	 * @param enabled The new enabled state.
	 */
	public void setEnabled(boolean enabled) {
		if (this.enabled != enabled) {
			this.enabled = enabled;
			onEnabledStateChanged();
		}
	}

	/**
	 * Called from {@link #setEnabled(boolean)} if the enabled
	 * state changed.
	 */
	protected void onEnabledStateChanged() {
	}

	/**
	 * Returns the parts enabled state.
	 *
	 * @return The enabled state.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Creates the part control(s).
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @param style The control style if applicable.
	 * @param span The horizontal span if applicable.
	 * @param toolkit The form toolkit or <code>null</code>.
	 */
	public abstract void createControl(Composite parent, int style, int span, FormToolkit toolkit);

	/**
	 * Convenience method to create a composite.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @param toolkit The form toolkit or <code>null</code>.
	 *
	 * @return The new composite.
	 */
	protected Composite createComposite(Composite parent, FormToolkit toolkit) {
		Assert.isNotNull(parent);
		return toolkit != null ? toolkit.createComposite(parent) : new Composite(parent, SWT.NONE);
	}

	/**
	 * Convenience method to create a "invisible" label for creating an
	 * empty space between controls.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @param span The horizontal span.
	 * @param toolkit The form toolkit or <code>null</code>.
	 *
	 * @return
	 */
	protected Label createEmptySpace(Composite parent, int span, FormToolkit toolkit) {
		Assert.isNotNull(parent);

		Label emptySpace = toolkit != null ? toolkit.createLabel(parent, null) : new Label(parent, SWT.NONE);

		GridData layoutData = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
		layoutData.horizontalSpan = span;
		layoutData.widthHint = 0; layoutData.heightHint = SWTControlUtil.convertHeightInCharsToPixels(emptySpace, 1);

		emptySpace.setLayoutData(layoutData);

		return emptySpace;
	}
}
