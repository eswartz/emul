/*
  ModuleInfoDialog.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.modules;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import v9t9.common.modules.IModule;
import v9t9.common.modules.ModuleInfo;
import ejs.base.settings.DialogSettingsWrapper;
import ejs.base.utils.TextUtils;

final class ModuleInfoDialog extends Dialog {
	/**
	 * 
	 */
	private final ModuleSelector moduleSelector;
	private final IModule module;

	ModuleInfoDialog(ModuleSelector moduleSelector, Shell parentShell, IModule module) {
		super(parentShell);
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
	}

	protected IDialogSettings getDialogBoundsSettings() {
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
		title.setText(module.getName());
		title.setFont(JFaceResources.getHeaderFont());
		
		ModuleInfo info = module.getInfo();
		String imagePath = info != null ? info.getImagePath() : null;
		title.setImage(this.moduleSelector.getOrLoadModuleImage(null, module, imagePath));
		
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
		
		CLabel summary = new CLabel(composite, SWT.WRAP);
		if (this.moduleSelector.isModuleLoadable(module)) {
			summary.setText("All module files resolved");
			summary.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_INFORMATION));
		} else {
			summary.setText("One or more module files are missing");
			summary.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_ERROR));
		}
		GridDataFactory.fillDefaults().grab(true, false).applyTo(summary);

		///////////
		
		final TreeViewer viewer = new TreeViewer(composite, SWT.BORDER);
		
		Tree tree = viewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		
		GridDataFactory.fillDefaults().grab(true,true).applyTo(tree);

		final TreeColumn nameColumn = new TreeColumn(tree, SWT.RIGHT);
		final TreeColumn infoColumn = new TreeColumn(tree, SWT.LEFT);

		ModuleDetailsContentProvider contentProvider = new ModuleDetailsContentProvider(this.moduleSelector.getMachine());
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(new ModuleDetailsTreeLabelProvider());
		
		viewer.setInput(contentProvider.createModuleContent(module));
		
		composite.getDisplay().asyncExec(new Runnable() {
			public void run() {
				viewer.expandToLevel(2);
				nameColumn.pack();
				infoColumn.pack();
			}
		});
		
		return composite;
	}

}