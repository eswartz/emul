/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.rcp.application;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.tm.te.rcp.application.activator.UIPlugin;
import org.eclipse.tm.te.rcp.application.nls.Messages;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.part.EditorInputTransfer;


/**
 * Target Explorer, RCP: Workbench window advisor implementation.
 */
public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {
	// (boolean) Prompt for exit confirmation when last window closed.
	public static final String EXIT_PROMPT_ON_CLOSE_LAST_WINDOW = "EXIT_PROMPT_ON_CLOSE_LAST_WINDOW"; //$NON-NLS-1$

	private ApplicationWorkbenchAdvisor wbAdvisor;
	private IEditorPart lastActiveEditor = null;
	private IPerspectiveDescriptor lastPerspective = null;

	private IWorkbenchPage lastActivePage;
	private String lastEditorTitle = ""; //$NON-NLS-1$

	private IPropertyListener editorPropertyListener = new IPropertyListener() {
		@SuppressWarnings("synthetic-access")
		public void propertyChanged(Object source, int propId) {
			if (propId == IWorkbenchPartConstants.PROP_TITLE) {
				if (lastActiveEditor != null) {
					String newTitle = lastActiveEditor.getTitle();
					if (!lastEditorTitle.equals(newTitle)) {
						recomputeTitle();
					}
				}
			}
		}
	};

	private IAdaptable lastInput;
	private IWorkbenchAction openPerspectiveAction;

	/**
	 * Crates a new IDE workbench window advisor.
	 *
	 * @param wbAdvisor the workbench advisor
	 * @param configurer the window configurer
	 */
	public ApplicationWorkbenchWindowAdvisor(ApplicationWorkbenchAdvisor wbAdvisor, IWorkbenchWindowConfigurer configurer) {
		super(configurer);
		this.wbAdvisor = wbAdvisor;
	}

	/**
	 * Returns the workbench.
	 *
	 * @return the workbench
	 */
	private IWorkbench getWorkbench() {
		return getWindowConfigurer().getWorkbenchConfigurer().getWorkbench();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#createActionBarAdvisor(org.eclipse.ui.application.IActionBarConfigurer)
	 */
	@Override
	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdvisor#preWindowShellClose
	 */
	@Override
	public boolean preWindowShellClose() {
		if (getWorkbench().getWorkbenchWindowCount() > 1) { return true; }
		// the user has asked to close the last window, while will cause the
		// workbench to close in due course - prompt the user for confirmation
		return promptOnExit(getWindowConfigurer().getWindow().getShell());
	}

	/**
	 * Asks the user whether the workbench should really be closed. Only asks if the preference is enabled.
	 *
	 * @param parentShell the parent shell to use for the confirmation dialog
	 * @return <code>true</code> if OK to exit, <code>false</code> if the user canceled
	 */
	static boolean promptOnExit(Shell parentShell) {
		boolean promptOnExit =  Platform.getPreferencesService().getBoolean(UIPlugin.getUniqueIdentifier(),
				EXIT_PROMPT_ON_CLOSE_LAST_WINDOW,
				false,
				null);

		if (promptOnExit) {
			String message;

			String productName = null;
			IProduct product = Platform.getProduct();
			if (product != null) {
				productName = product.getName();
			}
			if (productName == null) {
				message = Messages.PromptOnExitDialog_message0;
			} else {
				message = NLS.bind(Messages.PromptOnExitDialog_message1, productName);
			}

			MessageDialogWithToggle dlg = MessageDialogWithToggle.openOkCancelConfirm(	parentShell, Messages.PromptOnExitDialog_shellTitle,
					message,
					Messages.PromptOnExitDialog_choice, false, null, null);
			if (dlg.getReturnCode() != IDialogConstants.OK_ID) { return false; }
			if (dlg.getToggleState()) {
				new InstanceScope().getNode(UIPlugin.getUniqueIdentifier()).putBoolean(EXIT_PROMPT_ON_CLOSE_LAST_WINDOW, false);
			}
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.application.WorkbenchAdvisor#preWindowOpen
	 */
	@Override
	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();

		// show the shortcut bar and progress indicator, which are hidden by
		// default
		configurer.setShowPerspectiveBar(true);
		configurer.setShowFastViewBars(true);
		configurer.setShowProgressIndicator(true);

		// add the drag and drop support for the editor area
		configurer.addEditorAreaTransfer(EditorInputTransfer.getInstance());
		configurer.addEditorAreaTransfer(FileTransfer.getInstance());

		hookTitleUpdateListeners(configurer);
	}

	/**
	 * Hooks the listeners needed on the window
	 *
	 * @param configurer
	 */
	@SuppressWarnings("synthetic-access")
	private void hookTitleUpdateListeners(IWorkbenchWindowConfigurer configurer) {
		// hook up the listeners to update the window title
		configurer.getWindow().addPageListener(new IPageListener() {
			public void pageActivated(IWorkbenchPage page) {
				updateTitle(false);
			}

			public void pageClosed(IWorkbenchPage page) {
				updateTitle(false);
			}

			public void pageOpened(IWorkbenchPage page) {
				// do nothing
			}
		});
		configurer.getWindow().addPerspectiveListener(new PerspectiveAdapter() {
			@Override
			public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
				updateTitle(false);
			}

			@Override
			public void perspectiveSavedAs(IWorkbenchPage page, IPerspectiveDescriptor oldPerspective, IPerspectiveDescriptor newPerspective) {
				updateTitle(false);
			}

			@Override
			public void perspectiveDeactivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
				updateTitle(false);
			}
		});
		configurer.getWindow().getPartService().addPartListener(new IPartListener2() {
			public void partActivated(IWorkbenchPartReference ref) {
				if (ref instanceof IEditorReference) {
					updateTitle(false);
				}
			}

			public void partBroughtToTop(IWorkbenchPartReference ref) {
				if (ref instanceof IEditorReference) {
					updateTitle(false);
				}
			}

			public void partClosed(IWorkbenchPartReference ref) {
				updateTitle(false);
			}

			public void partDeactivated(IWorkbenchPartReference ref) {
				// do nothing
			}

			public void partOpened(IWorkbenchPartReference ref) {
				// do nothing
			}

			public void partHidden(IWorkbenchPartReference ref) {
				if (ref.getPart(false) == lastActiveEditor && lastActiveEditor != null) {
					updateTitle(true);
				}
			}

			public void partVisible(IWorkbenchPartReference ref) {
				if (ref.getPart(false) == lastActiveEditor && lastActiveEditor != null) {
					updateTitle(false);
				}
			}

			public void partInputChanged(IWorkbenchPartReference ref) {
				// do nothing
			}
		});
	}

	private String computeTitle() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		IWorkbenchPage currentPage = configurer.getWindow().getActivePage();
		IEditorPart activeEditor = null;
		if (currentPage != null) {
			activeEditor = lastActiveEditor;
		}

		String title = null;
		IProduct product = Platform.getProduct();
		if (product != null) {
			title = product.getName();
		}
		if (title == null) {
			title = ""; //$NON-NLS-1$
		}

		if (currentPage != null) {
			if (activeEditor != null) {
				lastEditorTitle = activeEditor.getTitleToolTip();
				title = NLS.bind("{0} - {1}", lastEditorTitle, title); //$NON-NLS-1$
			}
			IPerspectiveDescriptor persp = currentPage.getPerspective();
			String label = ""; //$NON-NLS-1$
			if (persp != null) {
				label = persp.getLabel();
			}
			IAdaptable input = currentPage.getInput();
			if (input != null && !input.equals(wbAdvisor.getDefaultPageInput())) {
				label = currentPage.getLabel();
			}
			if (label != null && !label.equals("")) { //$NON-NLS-1$
				title = NLS.bind("{0} - {1}", label, title); //$NON-NLS-1$
			}
		}

		return title;
	}

	private void recomputeTitle() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		String oldTitle = configurer.getTitle();
		String newTitle = computeTitle();
		if (!newTitle.equals(oldTitle)) {
			configurer.setTitle(newTitle);
		}
	}

	/**
	 * Updates the window title. Format will be: [pageInput -] [currentPerspective -] [editorInput -] [workspaceLocation
	 * -] productName
	 *
	 * @param editorHidden TODO
	 */
	private void updateTitle(boolean editorHidden) {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		IWorkbenchWindow window = configurer.getWindow();
		IEditorPart activeEditor = null;
		IWorkbenchPage currentPage = window.getActivePage();
		IPerspectiveDescriptor persp = null;
		IAdaptable input = null;

		if (currentPage != null) {
			activeEditor = currentPage.getActiveEditor();
			persp = currentPage.getPerspective();
			input = currentPage.getInput();
		}

		if (editorHidden) {
			activeEditor = null;
		}

		// Nothing to do if the editor hasn't changed
		if (activeEditor == lastActiveEditor && currentPage == lastActivePage && persp == lastPerspective && input == lastInput) { return; }

		if (lastActiveEditor != null) {
			lastActiveEditor.removePropertyListener(editorPropertyListener);
		}

		lastActiveEditor = activeEditor;
		lastActivePage = currentPage;
		lastPerspective = persp;
		lastInput = input;

		if (activeEditor != null) {
			activeEditor.addPropertyListener(editorPropertyListener);
		}

		recomputeTitle();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdvisor#createEmptyWindowContents(org.eclipse.ui.application.IWorkbenchWindowConfigurer,
	 *      org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createEmptyWindowContents(Composite parent) {
		final IWorkbenchWindow window = getWindowConfigurer().getWindow();
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		Display display = composite.getDisplay();
		Color bgCol = display.getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND);
		composite.setBackground(bgCol);
		Label label = new Label(composite, SWT.WRAP);
		label.setForeground(display.getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		label.setBackground(bgCol);
		label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		String msg = Messages.ApplicationWorkbenchAdvisor_noPerspective;
		label.setText(msg);
		ToolBarManager toolBarManager = new ToolBarManager();
		// TODO: should obtain the open perspective action from ActionFactory
		openPerspectiveAction = ActionFactory.OPEN_PERSPECTIVE_DIALOG.create(window);
		toolBarManager.add(openPerspectiveAction);
		ToolBar toolBar = toolBarManager.createControl(composite);
		toolBar.setBackground(bgCol);
		return composite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#dispose()
	 */
	@Override
	public void dispose() {
		if (openPerspectiveAction != null) {
			openPerspectiveAction.dispose();
			openPerspectiveAction = null;
		}
		super.dispose();
	}

}
