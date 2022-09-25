/*
  FileContentDialog.java

  (c) 2022 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import v9t9.common.files.IEmulatedFile;

/**
 * Show file text
 * @author ejs
 *
 */
public class FileTextDialog extends Dialog {

	private IEmulatedFile file;
	private FileTextComposite fileContent;

	/**
	 * @param shell
	 */
	public FileTextDialog(Shell shell) {
		super(shell);
		setShellStyle(getShellStyle() & ~(SWT.APPLICATION_MODAL + SWT.SYSTEM_MODAL) | SWT.MODELESS | SWT.RESIZE);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(800, 600);
	}
	
	public void setFile(IEmulatedFile file) {
		this.file = file;
		if (fileContent != null)
			fileContent.setFile(file);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		
		fileContent = new FileTextComposite(composite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(fileContent);
		
		fileContent.setFile(file);
		
		return composite;
	}
}
