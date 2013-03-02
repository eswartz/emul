/*
  FileContentDialog.java

  (c) 2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.gui.client.swt.shells;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import v9t9.common.files.EmulatedFile;

/**
 * @author ejs
 *
 */
public class FileContentDialog extends Dialog {

	private EmulatedFile file;
	private FileContentComposite fileContent;

	/**
	 * @param shell
	 */
	public FileContentDialog(Shell shell) {
		super(shell);
		setShellStyle(getShellStyle() & ~(SWT.APPLICATION_MODAL + SWT.SYSTEM_MODAL) | SWT.MODELESS | SWT.RESIZE);
	}

	public void setFile(EmulatedFile file) {
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
		
		fileContent = new FileContentComposite(composite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(fileContent);
		
		fileContent.setFile(file);
		
		return composite;
	}
}
