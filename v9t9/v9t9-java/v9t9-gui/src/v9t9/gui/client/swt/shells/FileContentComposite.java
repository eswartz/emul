/*
  FileContentComposite.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells;

import java.text.MessageFormat;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import v9t9.common.files.IEmulatedFile;
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
	
	public void setFile(IEmulatedFile file) {
		summaryLabel.setText(MessageFormat.format("File: {0}; Size = {1} sectors ({2} bytes)",
				file.getFileName(), (file.getSectorsUsed() + 1), file.getFileSize()));
		
		contentViewer.setFile(file);
	}
}
