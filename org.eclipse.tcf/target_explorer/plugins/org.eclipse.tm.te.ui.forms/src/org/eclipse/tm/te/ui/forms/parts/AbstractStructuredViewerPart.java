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
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Abstract structured viewer part implementation.
 */
public abstract class AbstractStructuredViewerPart extends AbstractPartWithButtons {
	// The structured viewer instance
	private StructuredViewer viewer = null;
	// The minimum size of the viewer control
	private Point minSize = null;

	/**
	 * Constructor.
	 *
	 * @param labels The list of label to apply to the created buttons in the given order. Must not be <code>null</code>.
	 */
	public AbstractStructuredViewerPart(String[] labels) {
		super(labels);
	}

	/**
	 * Returns the structured viewer instance.
	 *
	 * @return The structured viewer instance or <code>null</code>.
	 */
	public final StructuredViewer getViewer() {
		return viewer;
	}

	/**
	 * Set the minimum size of the viewer control.
	 *
	 * @param width The width in pixel.
	 * @param height The height in pixel.
	 */
	public void setMinSize(int width, int height) {
		minSize = new Point(width, height);
		onMinSizeChanged();
	}

	/**
	 * Called from {@link #setMinSize(int, int)} and {@link #createMainControl(Composite, int, int, FormToolkit)}
	 * to apply the minimum viewer control size.
	 */
	protected void onMinSizeChanged() {
		if (minSize != null && viewer != null) {
			GridData layoutData = (GridData)viewer.getControl().getLayoutData();
			layoutData.widthHint = minSize.x;
			layoutData.heightHint = minSize.y;
		}
	}

	/**
	 * Create the structured viewer instance.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @param style The viewer style.
	 * @param toolkit The form toolkit or <code>null</code>.
	 *
	 * @return The structured viewer instance.
	 */
	protected abstract StructuredViewer createStructuredViewer(Composite parent, int style, FormToolkit toolkit);

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.forms.parts.AbstractPartWithButtons#createMainControl(org.eclipse.swt.widgets.Composite, int, int, org.eclipse.ui.forms.widgets.FormToolkit)
	 */
	@Override
	protected void createMainControl(Composite parent, int style, int span, FormToolkit toolkit) {
		viewer = createStructuredViewer(parent, style, toolkit);
		Assert.isNotNull(viewer);

		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.horizontalSpan = span;
		viewer.getControl().setLayoutData(layoutData);

		onMinSizeChanged();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.forms.parts.AbstractPartWithButtons#onEnabledStateChanged()
	 */
	@Override
	protected void onEnabledStateChanged() {
		if (viewer != null) viewer.getControl().setEnabled(isEnabled());
		super.onEnabledStateChanged();
	}
}
