/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.views.internal.actions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.tm.te.ui.interfaces.IContextHelpIds;
import org.eclipse.tm.te.ui.interfaces.ImageConsts;
import org.eclipse.tm.te.ui.views.interfaces.IUIConstants;
import org.eclipse.tm.te.ui.views.internal.nls.Messages;
import org.eclipse.tm.te.ui.wizards.newWizard.NewWizardRegistry;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.actions.CommandAction;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.ui.navigator.WizardActionGroup;

/**
 * Action provider implementation providing the "New >" content menu
 * content.
 */
@SuppressWarnings("restriction")
public class NewActionProvider extends CommonActionProvider {
	// Reference to the action showing the "Other..." dialog (context menu)
	private CommandAction fNewWizardCommandAction = null;
	// Reference to the action showing the "Other..." dialog (toolbar)
	private CommandAction fNewWizardCommandActionToolbar = null;
	// Reference to the action group managing the context sensitive new wizards
	private WizardActionGroup fNewWizardActionGroup = null;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonActionProvider#init(org.eclipse.ui.navigator.ICommonActionExtensionSite)
	 */
	@Override
	public void init(ICommonActionExtensionSite site) {
		super.init(site);

		if (site.getViewSite() instanceof ICommonViewerWorkbenchSite) {
			// To initialize the actions, the workbench window instance is required
			IWorkbenchWindow window = ((ICommonViewerWorkbenchSite)site.getViewSite()).getWorkbenchWindow();
			// Initialize the actions
			fNewWizardCommandAction = new CommandAction(window, "org.eclipse.tm.te.ui.command.newWizards"); //$NON-NLS-1$
			fNewWizardCommandAction.setImageDescriptor(null);
			fNewWizardCommandAction.setDisabledImageDescriptor(null);
			fNewWizardCommandAction.setText(Messages.NewActionProvider_NewWizardCommandAction_label);
			fNewWizardCommandAction.setToolTipText(Messages.NewActionProvider_NewWizardCommandAction_tooltip);
            window.getWorkbench().getHelpSystem().setHelp(fNewWizardCommandAction, IContextHelpIds.NEW_TARGET_WIZARD);

			fNewWizardCommandActionToolbar = new CommandAction(window, "org.eclipse.tm.te.ui.command.newWizards"); //$NON-NLS-1$
			fNewWizardCommandActionToolbar.setImageDescriptor(org.eclipse.tm.te.ui.activator.UIPlugin.getImageDescriptor(ImageConsts.IMAGE_NEW_TARGET_WIZARD_ENABLED));
			fNewWizardCommandActionToolbar.setDisabledImageDescriptor(org.eclipse.tm.te.ui.activator.UIPlugin.getImageDescriptor(ImageConsts.IMAGE_NEW_TARGET_WIZARD_DISABLED));
			fNewWizardCommandActionToolbar.setText(Messages.NewActionProvider_NewWizardCommandAction_label);
			fNewWizardCommandActionToolbar.setToolTipText(Messages.NewActionProvider_NewWizardCommandAction_tooltip);
            window.getWorkbench().getHelpSystem().setHelp(fNewWizardCommandActionToolbar, IContextHelpIds.NEW_TARGET_WIZARD);

			fNewWizardActionGroup = new WizardActionGroup(window,
														  NewWizardRegistry.getInstance(),
														  WizardActionGroup.TYPE_NEW,
														  site.getContentService());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#dispose()
	 */
	@Override
	public void dispose() {
		if (fNewWizardCommandAction != null) {
			fNewWizardCommandAction.dispose();
			fNewWizardCommandAction = null;
		}
		if (fNewWizardActionGroup != null) {
			fNewWizardActionGroup.dispose();
			fNewWizardActionGroup = null;
		}
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	public void fillContextMenu(IMenuManager menu) {
		// If none of the actions got created, there is nothing to do here
		if (fNewWizardCommandAction == null && fNewWizardActionGroup == null) {
			return;
		}

		// Create the new sub menu
		IMenuManager newMenu = new MenuManager(Messages.NewActionProvider_NewMenu_label,
											   org.eclipse.tm.te.ui.activator.UIPlugin.getImageDescriptor(ImageConsts.IMAGE_NEW_TARGET_WIZARD_ENABLED),
											   IUIConstants.ID_EXPLORER + ".menu.new"); //$NON-NLS-1$

		// Add the context sensitive wizards (commonWizard element)
		if (fNewWizardActionGroup != null) {
			fNewWizardActionGroup.setContext(getContext());
			fNewWizardActionGroup.fillContextMenu(newMenu);
		}

		// Add the standard additions marker
		newMenu.add(new Separator(ICommonMenuConstants.GROUP_ADDITIONS));

		// Add the "Other..." dialog action
		if (fNewWizardCommandAction != null) {
			newMenu.add(new Separator());
			newMenu.add(fNewWizardCommandAction);
		}

		// The menu will be appended after the GROUP_NEW group.
		menu.insertAfter(ICommonMenuConstants.GROUP_NEW, newMenu);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
	 */
	@Override
	public void fillActionBars(IActionBars actionBars) {
		if (fNewWizardCommandActionToolbar == null) {
			return;
		}

		IToolBarManager toolbar = actionBars.getToolBarManager();
		toolbar.insertAfter(ICommonMenuConstants.GROUP_NEW, fNewWizardCommandActionToolbar);
	}
}
