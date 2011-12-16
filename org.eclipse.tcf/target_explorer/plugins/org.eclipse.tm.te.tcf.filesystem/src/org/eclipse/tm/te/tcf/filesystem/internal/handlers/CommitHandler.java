/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * William Chen (Wind River)- [345552] Edit the remote files with a proper editor
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.internal.handlers;

import java.io.File;

import org.eclipse.compare.CompareUI;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.te.tcf.filesystem.internal.compare.LocalTypedElement;
import org.eclipse.tm.te.tcf.filesystem.internal.compare.MergeEditorInput;
import org.eclipse.tm.te.tcf.filesystem.internal.compare.RemoteTypedElement;
import org.eclipse.tm.te.tcf.filesystem.internal.exceptions.TCFException;
import org.eclipse.tm.te.tcf.filesystem.internal.nls.Messages;
import org.eclipse.tm.te.tcf.filesystem.model.CacheState;
import org.eclipse.tm.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The handler that commits the content of a modified file to the target file system.
 */
public class CommitHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelectionChecked(event);
		FSTreeNode node = (FSTreeNode) selection.getFirstElement();
		try {
			// Refresh the node to get the latest state.
			StateManager.getInstance().refreshState(node);
			Shell parent = HandlerUtil.getActiveShell(event);
			File file = CacheManager.getInstance().getCacheFile(node);
			if (file.exists()) {
				CacheState state = StateManager.getInstance().getCacheState(node);
				switch (state) {
				case conflict:
					String title = Messages.CmmitHandler_StateChangedDialogTitle;
					String message = NLS.bind(Messages.CmmitHandler_StateChangedMessage, node.name);
					MessageDialog msgDialog = new MessageDialog(parent, title, null, message, MessageDialog.QUESTION, new String[] { Messages.CmmitHandler_Merge, Messages.CmmitHandler_CommitAnyway,
							Messages.CmmitHandler_Cancel }, 0);
					int index = msgDialog.open();
					if (index == 0) {// Merge
						LocalTypedElement local = new LocalTypedElement(node);
						RemoteTypedElement remote = new RemoteTypedElement(node);
						IWorkbenchPage page = HandlerUtil.getActiveSite(event).getPage();
						MergeEditorInput input = new MergeEditorInput(local, remote, page);
						CompareUI.openCompareDialog(input);
					} else if (index == 1) { // Commit anyway
						CacheManager.getInstance().upload(node);
					}
					break;
				case modified:
					// Commit it normally.
					CacheManager.getInstance().upload(node);
					break;
				case consistent:
					break;
				case outdated:
					break;
				}
			} else {
				String message = NLS.bind(Messages.CmmitHandler_FileDeleted, file.getName());
				MessageDialog.openError(parent, Messages.CmmitHandler_ErrorTitle, message);
			}
		} catch (TCFException e) {
			Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			MessageDialog.openError(parent, Messages.StateManager_RefreshFailureTitle, e.getLocalizedMessage());
		}
		return null;
	}

}
