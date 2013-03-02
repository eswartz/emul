/*
  FileContentComposite.java

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

import java.text.MessageFormat;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import v9t9.common.files.EmulatedFile;
import v9t9.gui.client.swt.shells.disk.ByteContentViewer;

/**
 * @author ejs
 *
 */
public class FileContentComposite extends Composite {

	private Label summaryLabel;
	private ByteContentViewer contentViewer;

	public FileContentComposite(Composite parent, int style) {
		super(parent, style);
		
		GridLayoutFactory.fillDefaults().applyTo(this);
		
		summaryLabel = new Label(this, SWT.WRAP);
		summaryLabel.setText("No file");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(summaryLabel);
		
		contentViewer = new ByteContentViewer(this, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(contentViewer);
	}
	
	public void setFile(EmulatedFile file) {
		summaryLabel.setText(MessageFormat.format("File: {0}; Size = {1} sectors ({2} bytes)",
				file.getFileName(), (file.getSectorsUsed() + 1), file.getFileSize()));
		
		contentViewer.setFile(file);
	}
}
