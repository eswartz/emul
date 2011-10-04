/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * William Chen (Wind River)- [345387]Open the remote files with a proper editor
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.internal.handlers;

import java.io.File;

import org.eclipse.compare.CompareUI;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.te.tcf.filesystem.internal.compare.LocalTypedElement;
import org.eclipse.tm.te.tcf.filesystem.internal.compare.MergeEditorInput;
import org.eclipse.tm.te.tcf.filesystem.internal.compare.RemoteTypedElement;
import org.eclipse.tm.te.tcf.filesystem.internal.exceptions.TCFException;
import org.eclipse.tm.te.tcf.filesystem.internal.nls.Messages;
import org.eclipse.tm.te.tcf.filesystem.model.CacheState;
import org.eclipse.tm.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;

/**
 * The action handler to open a file on the remote file system.
 */
public class OpenFileHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelectionChecked(event);
		final FSTreeNode node = (FSTreeNode) selection.getFirstElement();
		IWorkbenchPage page = HandlerUtil.getActiveSite(event).getPage();
		if (ContentTypeHelper.getInstance().isBinaryFile(node)) {
			// If the file is a binary file.
			Shell parent = HandlerUtil.getActiveShell(event);
			MessageDialog.openWarning(parent, Messages.OpenFileHandler_Warning,
					Messages.OpenFileHandler_OpeningBinaryNotSupported);
		} else {
			// Open the file node.
			openFile(node, page);
		}
		return null;
	}

	/**
	 * Open the file node in an editor of the specified workbench page. If the
	 * local cache file of the node is stale, then download it. Then open its
	 * local cache file.
	 *
	 * @param node
	 *            The file node to be opened.
	 * @param page
	 *            The workbench page in which the editor is opened.
	 */
	private void openFile(FSTreeNode node, IWorkbenchPage page) {
		File file = CacheManager.getInstance().getCacheFile(node);
		if (!file.exists()) {
			// If the file node's local cache does not exist yet, download it.
			boolean successful = CacheManager.getInstance().download(node);
			if (!successful) {
				return;
			}
		}
		if (!CacheManager.getInstance().isAutoSaving()) {
			openEditor(page, node);
		} else {
			try {
				StateManager.getInstance().refreshState(node);
			} catch (TCFException e) {
				Shell parent = page.getWorkbenchWindow().getShell();
				MessageDialog.openError(parent, Messages.StateManager_RefreshFailureTitle, e.getLocalizedMessage());
				return;
			}
			CacheState state = StateManager.getInstance().getCacheState(node);
			switch (state) {
			case consistent:
				openEditor(page, node);
				break;
			case modified: {
				// If the file node's local cache has been modified, upload it
				// before open it.
				boolean successful = CacheManager.getInstance().upload(node);
				if (successful)
					openEditor(page, node);
			}
				break;
			case outdated: {
				// If the file node's local cache does not exist yet, download
				// it.
				boolean successful = CacheManager.getInstance().download(node);
				if (successful)
					openEditor(page, node);
			}
				break;
			case conflict: {
				String title = Messages.OpenFileHandler_ConflictingTitle;
				String message = NLS.bind(Messages.OpenFileHandler_ConflictingMessage, node.name);
				Shell parent = page.getWorkbenchWindow().getShell();
				MessageDialog msgDialog = new MessageDialog(parent, title, null, message, MessageDialog.QUESTION, new String[] { Messages.OpenFileHandler_Merge, Messages.OpenFileHandler_OpenAnyway,
						Messages.OpenFileHandler_Cancel }, 0);
				int index = msgDialog.open();
				if (index == 0) {
					LocalTypedElement local = new LocalTypedElement(node);
					RemoteTypedElement remote = new RemoteTypedElement(node);
					MergeEditorInput input = new MergeEditorInput(local, remote, page);
					CompareUI.openCompareDialog(input);
				} else if (index == 1) {
					openEditor(page, node);
				}
			}
				break;
			}
		}
	}

	/**
	 * Open the editor to display the file node in the UI thread.
	 *
	 * @param page
	 *            The workbench page in which the editor is opened.
	 * @param node
	 *            The file node whose local cache file is opened.
	 */
	private void openEditor(final IWorkbenchPage page, final FSTreeNode node) {
		Display display = page.getWorkbenchWindow().getWorkbench().getDisplay();
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				IPath path = CacheManager.getInstance().getCachePath(node);
				IFileStore fileStore = EFS.getLocalFileSystem().getStore(path);
				try {
					IDE.openEditorOnFileStore(page, fileStore);
				} catch (PartInitException e) {
				}
			}
		});
	}
}
