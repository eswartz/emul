/*
  ModuleInfoDialog.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.modules;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Collections;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import v9t9.common.events.NotifyException;
import v9t9.common.machine.IMachine;
import v9t9.common.modules.IModule;
import v9t9.common.modules.ModuleDatabase;
import v9t9.common.modules.ModuleInfo;
import ejs.base.settings.DialogSettingsWrapper;
import ejs.base.utils.TextUtils;

final class ModuleInfoDialog extends Dialog {
	/**
	 * 
	 */
	private final ModuleSelector moduleSelector;
	private final IModule module;
	private IMachine machine;
	private ModuleImages images;

	ModuleInfoDialog(IMachine machine, ModuleSelector moduleSelector, Shell parentShell, IModule module) {
		super(parentShell);
		this.machine = machine;
		this.moduleSelector = moduleSelector;
		this.module = module;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#setShellStyle(int)
	 */
	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(newShellStyle | SWT.RESIZE | SWT.TOOL | SWT.CLOSE);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		final Button button = createButton(parent, IDialogConstants.CLIENT_ID,
				"Copy module XML", false);
		((GridData) button.getLayoutData()).horizontalAlignment = SWT.LEFT;
		((GridData) button.getLayoutData()).grabExcessHorizontalSpace = true;
		
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Clipboard clipboard = new Clipboard(button.getDisplay());
				
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				try {
					ModuleDatabase.saveModuleListAndClose(machine.getMemory(), 
							bos, null, 
							Collections.singletonList(module));
				} catch (NotifyException e1) {
					e1.printStackTrace();
					MessageDialog.openError(getShell(), "Error", "Failed to copy module text to clipboard");
					return;
				}
				
				String textData = bos.toString();
				
				TextTransfer textTransfer = TextTransfer.getInstance();
				Transfer[] transfers = { textTransfer };
				Object[] data = { textData };
				clipboard.setContents(data, transfers);
				
				clipboard.dispose();
			}
		});

		
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Module Details");
		this.images = new ModuleImages(newShell.getDisplay(), machine);
		
		newShell.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				images.dispose();
			}
		});
	}

	protected IDialogSettings getDialogBoundsSettings() {
		if (moduleSelector == null)
			return super.getDialogBoundsSettings();
		return new DialogSettingsWrapper(this.moduleSelector.getDialogSettings().findOrAddSection("ModuleInfo"));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
	 */
	@Override
	protected Point getInitialSize() {
		Point sz = super.getInitialSize();
		if (sz.x < 600)
			sz.x = 600;
		return sz;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		///////////
		
		CLabel title = new CLabel(composite, SWT.NONE);
		title.setText(module.getName().replace("&", "&&"));
		title.setFont(JFaceResources.getHeaderFont());

		ModuleInfo info = module.getInfo();
		String imagePath = null;
		Image image = null;
		if (moduleSelector != null) {
			imagePath = info != null ? info.getImagePath() : null;
			if (imagePath != null) {
				URI imageURI = images.getImageURI(imagePath);
				if (imageURI != null)
					image = images.loadImage(imageURI);
			}
		}
		if (image == null) {
			image = images.loadImage("stock_module.png");
		}
		title.setImage(image);
		
		GridDataFactory.fillDefaults().grab(true, false).applyTo(title);
		
		///////////
		Label sep;
		
		sep = new Label(composite, SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sep);

		if (info != null && !TextUtils.isEmpty(info.getDescription())) {
			Label descr = new Label(composite, SWT.WRAP);
			descr.setText(info.getDescription());
			GridDataFactory.fillDefaults().grab(true, false).applyTo(descr);
		
			sep = new Label(composite, SWT.HORIZONTAL);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(sep);
		}
		
		///////////
		
		if (moduleSelector != null) {
			CLabel summary = new CLabel(composite, SWT.WRAP);
			if (this.moduleSelector.isModuleLoadable(module)) {
				summary.setText("All module files resolved");
				summary.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_INFORMATION));
			} else {
				summary.setText("One or more module files are missing");
				summary.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_ERROR));
			}
			GridDataFactory.fillDefaults().grab(true, false).applyTo(summary);
		}
		
		///////////
		
		final TreeViewer viewer = new TreeViewer(composite, SWT.BORDER);
		
		Tree tree = viewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		
		GridDataFactory.fillDefaults().grab(true,true).applyTo(tree);

		final TreeColumn nameColumn = new TreeColumn(tree, SWT.RIGHT);
		final TreeColumn infoColumn = new TreeColumn(tree, SWT.LEFT);

		ModuleDetailsContentProvider contentProvider = new ModuleDetailsContentProvider(machine);
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(new ModuleDetailsTreeLabelProvider());
		
		viewer.setAutoExpandLevel(3);
		viewer.setInput(contentProvider.createModuleContent(module));
		
		composite.getDisplay().asyncExec(new Runnable() {
			public void run() {
				nameColumn.pack();
				infoColumn.pack();
			}
		});
		
		return composite;
	}

}