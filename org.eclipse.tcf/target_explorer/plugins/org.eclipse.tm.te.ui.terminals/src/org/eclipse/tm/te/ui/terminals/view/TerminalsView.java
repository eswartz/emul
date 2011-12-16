/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.terminals.view;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tm.te.ui.terminals.interfaces.ITerminalsView;
import org.eclipse.tm.te.ui.terminals.tabs.TabFolderManager;
import org.eclipse.tm.te.ui.terminals.tabs.TabFolderMenuHandler;
import org.eclipse.tm.te.ui.terminals.tabs.TabFolderToolbarHandler;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

/**
 * Terminals view.
 */
public class TerminalsView extends ViewPart implements ITerminalsView {

	// Reference to the main page book control
	private PageBook pageBookControl;
	// Reference to the tab folder maintaining the consoles
	private CTabFolder tabFolderControl;
	// Reference to the tab folder manager
	private TabFolderManager tabFolderManager;
	// Reference to the tab folder menu handler
	private TabFolderMenuHandler tabFolderMenuHandler;
	// Reference to the tab folder toolbar handler
	private TabFolderToolbarHandler tabFolderToolbarHandler;
	// Reference to the empty page control (to be show if no console is open)
	private Control emptyPageControl;

	/**
	 * Constructor.
	 */
	public TerminalsView() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		// Dispose the tab folder manager
		if (tabFolderManager != null) { tabFolderManager.dispose(); tabFolderManager = null; }
		// Dispose the tab folder menu handler
		if (tabFolderMenuHandler != null) { tabFolderMenuHandler.dispose(); tabFolderMenuHandler = null; }
		// Dispose the tab folder toolbar handler
		if (tabFolderToolbarHandler != null) { tabFolderToolbarHandler.dispose(); tabFolderToolbarHandler = null; }

		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		// Create the page book control
		pageBookControl = doCreatePageBookControl(parent);
		Assert.isNotNull(pageBookControl);
		// Configure the page book control
		doConfigurePageBookControl(pageBookControl);

		// Create the empty page control
		emptyPageControl = doCreateEmptyPageControl(pageBookControl);
		Assert.isNotNull(emptyPageControl);
		// Configure the empty page control
		doConfigureEmptyPageControl(emptyPageControl);

		// Create the tab folder control (empty)
		tabFolderControl = doCreateTabFolderControl(pageBookControl);
		Assert.isNotNull(tabFolderControl);
		// Configure the tab folder control
		doConfigureTabFolderControl(tabFolderControl);

		// Create the tab folder manager
		tabFolderManager = doCreateTabFolderManager(this);
		Assert.isNotNull(tabFolderManager);
		// Set the tab folder manager as the selection provider
		getSite().setSelectionProvider(tabFolderManager);

		// Setup the tab folder menu handler
		tabFolderMenuHandler = doCreateTabFolderMenuHandler(this);
		Assert.isNotNull(tabFolderMenuHandler);
		doConfigureTabFolderMenuHandler(tabFolderMenuHandler);

		// Setup the tab folder toolbar handler
		tabFolderToolbarHandler = doCreateTabFolderToolbarHandler(this);
		Assert.isNotNull(tabFolderToolbarHandler);
		doConfigureTabFolderToolbarHandler(tabFolderToolbarHandler);

		// Show the empty page control by default
		switchToEmptyPageControl();
	}

	/**
	 * Creates the {@link PageBook} instance.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @return The page book instance. Must never be <code>null</code>.
	 */
	protected PageBook doCreatePageBookControl(Composite parent) {
		return new PageBook(parent, SWT.NONE);
	}

	/**
	 * Configure the given page book control.
	 *
	 * @param pagebook The page book control. Must not be <code>null</code>.
	 */
	protected void doConfigurePageBookControl(PageBook pagebook) {
		Assert.isNotNull(pagebook);

		if (getContextHelpId() != null)
			PlatformUI.getWorkbench().getHelpSystem().setHelp(pagebook, getContextHelpId());
	}

	/**
	 * Returns the context help id associated with the terminal console view instance.
	 * <p>
	 * <b>Note:</b> The default implementation returns the view id as context help id.
	 *
	 * @return The context help id or <code>null</code> if none is associated.
	 */
	@Override
	public String getContextHelpId() {
		return getViewSite().getId();
	}

	/**
	 * Creates the empty page control instance.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @return The empty page control instance. Must never be <code>null</code>.
	 */
	protected Control doCreateEmptyPageControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		return composite;
	}

	/**
	 * Configures the empty page control.
	 *
	 * @param control The empty page control. Must not be <code>null</code>.
	 */
	protected void doConfigureEmptyPageControl(Control control) {
		Assert.isNotNull(control);
	}

	/**
	 * Creates the tab folder control instance.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @return The tab folder control instance. Must never be <code>null</code>.
	 */
	protected CTabFolder doCreateTabFolderControl(Composite parent) {
		return new CTabFolder(parent, SWT.NO_REDRAW_RESIZE | SWT.NO_TRIM | SWT.FLAT | SWT.BORDER);
	}

	/**
	 * Configures the tab folder control.
	 *
	 * @param tabFolder The tab folder control. Must not be <code>null</code>.
	 */
	protected void doConfigureTabFolderControl(CTabFolder tabFolder) {
		Assert.isNotNull(tabFolder);

		// Set the layout data
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Set the tab gradient coloring from the global preferences
		if (useGradientTabBackgroundColor()) {
			tabFolder.setSelectionBackground(new Color[] {
					JFaceResources.getColorRegistry().get("org.eclipse.ui.workbench.ACTIVE_TAB_BG_START"), //$NON-NLS-1$
					JFaceResources.getColorRegistry().get("org.eclipse.ui.workbench.ACTIVE_TAB_BG_END") //$NON-NLS-1$
			},
			new int[] {100}, true);
		}
		// Apply the tab folder selection foreground color
		tabFolder.setSelectionForeground(JFaceResources.getColorRegistry().get("org.eclipse.ui.workbench.ACTIVE_TAB_TEXT_COLOR")); //$NON-NLS-1$

		// Set the tab style from the global preferences
		tabFolder.setSimple(PlatformUI.getPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS));
	}

	/**
	 * If <code>True</code> is returned, the inner tabs are colored with
	 * gradient coloring set in the Eclipse workbench color settings.
	 *
	 * @return <code>True</code> to use gradient tab colors, <code>false</code> otherwise.
	 */
	protected boolean useGradientTabBackgroundColor() {
		return false;
	}

	/**
	 * Creates the tab folder manager.
	 *
	 * @param parentView The parent view instance. Must be not <code>null</code>.
	 * @return The tab folder manager. Must never be <code>null</code>.
	 */
	protected TabFolderManager doCreateTabFolderManager(ITerminalsView parentView) {
		Assert.isNotNull(parentView);
		return new TabFolderManager(parentView);
	}

	/**
	 * Creates the tab folder menu handler.
	 *
	 * @param parentView The parent view instance. Must be not <code>null</code>.
	 * @return The tab folder menu handler. Must never be <code>null</code>.
	 */
	protected TabFolderMenuHandler doCreateTabFolderMenuHandler(ITerminalsView parentView) {
		Assert.isNotNull(parentView);
		return new TabFolderMenuHandler(parentView);
	}

	/**
	 * Configure the tab folder menu handler
	 *
	 * @param menuHandler The tab folder menu handler. Must not be <code>null</code>.
	 */
	protected void doConfigureTabFolderMenuHandler(TabFolderMenuHandler menuHandler) {
		Assert.isNotNull(menuHandler);
		menuHandler.initialize();
	}

	/**
	 * Creates the tab folder toolbar handler.
	 *
	 * @param parentView The parent view instance. Must be not <code>null</code>.
	 * @return The tab folder toolbar handler. Must never be <code>null</code>.
	 */
	protected TabFolderToolbarHandler doCreateTabFolderToolbarHandler(ITerminalsView parentView) {
		Assert.isNotNull(parentView);
		return new TabFolderToolbarHandler(parentView);
	}

	/**
	 * Configure the tab folder toolbar handler
	 *
	 * @param toolbarHandler The tab folder toolbar handler. Must not be <code>null</code>.
	 */
	protected void doConfigureTabFolderToolbarHandler(TabFolderToolbarHandler toolbarHandler) {
		Assert.isNotNull(toolbarHandler);
		toolbarHandler.initialize();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		pageBookControl.setFocus();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.terminals.interfaces.ITerminalsView#switchToEmptyPageControl()
	 */
	@Override
	public void switchToEmptyPageControl() {
		if (!pageBookControl.isDisposed() && !emptyPageControl.isDisposed()) pageBookControl.showPage(emptyPageControl);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.terminals.interfaces.ITerminalsView#switchToTabFolderControl()
	 */
	@Override
	public void switchToTabFolderControl() {
		if (!pageBookControl.isDisposed() && !tabFolderControl.isDisposed()) pageBookControl.showPage(tabFolderControl);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (CTabFolder.class.isAssignableFrom(adapter)) {
			return tabFolderControl;
		} else if (TabFolderManager.class.isAssignableFrom(adapter)) {
			return tabFolderManager;
		} else if (TabFolderMenuHandler.class.isAssignableFrom(adapter)) {
			return tabFolderMenuHandler;
		} else if (TabFolderToolbarHandler.class.isAssignableFrom(adapter)) {
			return tabFolderToolbarHandler;
		}

		return super.getAdapter(adapter);
	}
}
