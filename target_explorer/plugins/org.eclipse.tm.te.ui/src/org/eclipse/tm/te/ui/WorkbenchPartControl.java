/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm.te.ui.activator.UIPlugin;
import org.eclipse.tm.te.ui.forms.CustomFormToolkit;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;


/**
 * Target Explorer: Common workbench part control implementation.
 */
public class WorkbenchPartControl extends PlatformObject {
	/**
	 * Reference to the parent workbench part the control might be embedded in.
	 */
	private final IWorkbenchPart fParentPart;

	/**
	 * Reference to the form toolkit instance provided via {@link #setupFormPanel(Composite, CustomFormToolkit)}.
	 */
	private CustomFormToolkit fFormToolkit = null;

	/**
	 * Reference to the parent control.
	 */
	private Composite fParentControl;

	/**
	 * Constructor.
	 */
	public WorkbenchPartControl() {
		this(null);
	}

	/**
	 * Constructor.
	 *
	 * @param parentPart The parent workbench part this control is embedded in or <code>null</code>.
	 */
	public WorkbenchPartControl(IWorkbenchPart parentPart) {
		super();
		fParentPart = parentPart;
	}

	/**
	 * Returns the parent workbench part the control might be embedded in.
	 *
	 * @return The parent workbench part or <code>null</code>.
	 */
	public final IWorkbenchPart getParentPart() {
		return fParentPart;
	}

	/**
	 * Returns if the <code>setupPanel(...)</code> method has been called at least once with
	 * a non-null parent control.
	 *
	 * @return <code>true</code> if the associated parent control is not <code>null</code>, <code>false</code> otherwise.
	 */
	public final boolean isControlCreated() {
		return (fParentControl != null);
	}

	/**
	 * Returns the parent control of the control.
	 *
	 * @return The parent control or <code>null</code>.
	 */
	public final Composite getParentControl() {
		return fParentControl;
	}

	/**
	 * Cleanup all resources the control might have been created.
	 */
	public void dispose() {
		fParentControl = null;
	}

	/**
	 * Creates the controls UI elements.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @param toolkit The {@link CustomFormToolkit} instance. Must not be <code>null</code>.
	 */
	public void setupFormPanel(Composite parent, CustomFormToolkit toolkit) {
		assert parent != null && toolkit != null;
		fParentControl = parent;
		fFormToolkit = toolkit;
	}

	/**
	 * Returns the associated form toolkit instance.
	 *
	 * @return The form toolkit instance or <code>null</code> if not initialized yet.
	 */
	protected final CustomFormToolkit getFormToolkit() {
		return fFormToolkit;
	}

	/**
	 * Returns the selection service of the workbench.
	 *
	 * @return The selection service or <code>null</code>.
	 */
	protected final ISelectionService getSelectionService() {
		ISelectionService selectionService = null;
		// Check if plugin, workbench and active workbench window are still valid
		if (UIPlugin.getDefault() != null && UIPlugin.getDefault().getWorkbench() != null
			&& UIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow() != null) {
			selectionService = UIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		}
		return selectionService;
	}

}
