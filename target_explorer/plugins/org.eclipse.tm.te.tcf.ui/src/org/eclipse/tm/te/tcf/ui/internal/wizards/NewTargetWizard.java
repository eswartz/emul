/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.internal.wizards;

import java.util.Map;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.te.tcf.core.Tcf;
import org.eclipse.tm.te.tcf.core.interfaces.IChannelManager;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.tcf.locator.interfaces.services.ILocatorModelLookupService;
import org.eclipse.tm.te.tcf.ui.internal.model.Model;
import org.eclipse.tm.te.tcf.ui.internal.nls.Messages;
import org.eclipse.tm.te.tcf.ui.internal.wizards.pages.NewTargetWizardPage;
import org.eclipse.tm.te.ui.views.interfaces.IUIConstants;
import org.eclipse.tm.te.ui.wizards.AbstractWizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

/**
 * New TCF target wizard implementation.
 */
public class NewTargetWizard extends AbstractWizard implements INewWizard {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// Set the window title
		setWindowTitle(Messages.NewTargetWizard_windowTitle);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		// Create and add the wizard pages
		addPage(new NewTargetWizardPage());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		IWizardPage page = getPage(NewTargetWizardPage.class.getName());
		if (page instanceof NewTargetWizardPage) {
			// Trigger the saving of the widget history
			((NewTargetWizardPage)page).saveWidgetValues();
			// Get the peer attributes map from the page
			Map<String, String> peerAttributes = ((NewTargetWizardPage)page).getPeerAttributes();
			if (peerAttributes != null) {
				// Try to connect to the peer
				IChannel channel = null;
				try {
					Tcf.getChannelManager().openChannel(peerAttributes, new IChannelManager.DoneOpenChannel() {
						public void doneOpenChannel(Throwable error, IChannel channel) {
							// We ignore the error here, because we don't present it to the user
							if (channel != null && channel.getRemotePeer() != null) {
								IPeer peer = channel.getRemotePeer();
								ILocatorModel model = Model.getModel();
								if (model != null) {
									final IPeerModel peerNode = model.getService(ILocatorModelLookupService.class).lkupPeerModelById(peer.getID());
									if (peerNode != null && PlatformUI.isWorkbenchRunning()) {
										PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
											public void run() {
												if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null && PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null) {
													IViewReference reference = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(IUIConstants.ID_EXPLORER);
													IWorkbenchPart part = reference != null ? reference.getPart(false) : null;
													ISelectionProvider selectionProvider = part != null && part.getSite() != null ? part.getSite().getSelectionProvider() : null;
													if (selectionProvider != null) selectionProvider.setSelection(new StructuredSelection(peerNode));
												}
											}
										});
									}
								}
							}

							if (channel != null) channel.close();
						}
					});
				} catch (Exception e) {
				} finally {
					// Close the channel again
					if (channel != null) {
						final IChannel finChannel = channel;
						if (Protocol.isDispatchThread()) {
							finChannel.close();
						} else {
							Protocol.invokeAndWait(new Runnable() {
								public void run() {
									finChannel.close();
								}
							});
						}
					}
				}
			}
		}

		return true;
	}

}
