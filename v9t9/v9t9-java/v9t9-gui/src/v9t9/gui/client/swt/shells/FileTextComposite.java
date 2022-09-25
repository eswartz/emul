/*
  FileContentComposite.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ejs.base.utils.HexUtils;
import v9t9.common.files.DsrException;
import v9t9.common.files.IEmulatedFile;
import v9t9.common.files.NativeTextFile;
import v9t9.common.files.PabConstants;
import v9t9.common.memory.ByteMemoryAccess;
import v9t9.engine.files.directory.OpenFile;

/**
 * @author ejs
 *
 */
public class FileTextComposite extends Composite {

	private Label summaryLabel;
	private Text text;

	public FileTextComposite(Composite parent, int style) {
		super(parent, style);
		
		GridLayoutFactory.fillDefaults().applyTo(this);
		
		summaryLabel = new Label(this, SWT.WRAP);
		summaryLabel.setText("No file");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(summaryLabel);
		
		text = new Text(this, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(text);
	}
	
	public void setFile(IEmulatedFile file) {
		summaryLabel.setText(MessageFormat.format("File: {0}; Size = {1} sectors ({2} bytes)",
				file.getFileName(), (file.getSectorsUsed() + 1), file.getFileSize()));
		
		try {
			StringBuilder sb = new StringBuilder();
			for (byte[] line : readRecords(file)) {
				sb.append(convertText(line)).append('\n');
			}
			text.setText(sb.toString());
		} catch (IOException e) {
			text.setForeground(text.getDisplay().getSystemColor(SWT.COLOR_RED));
			text.setText("Failed to read " + file + ":\n" + e.getMessage());
		}
	}
	
	private String convertText(byte[] line) {
		StringBuilder sb = new StringBuilder();
		for (byte ch : line) {
			if (ch < 32 || ch >= 127) {
				sb.append("\\x").append(HexUtils.padByte(Integer.toHexString(ch & 0xff)));
			} else {
				sb.append((char) ch);
			}
		}
		return sb.toString();
	}

	private List<byte[]> readRecords(IEmulatedFile file) throws IOException {
		OpenFile of = new OpenFile(file, file.getFileName());
		List<byte[]> lines = new ArrayList<byte[]>(); 
		try {
			if (file instanceof NativeTextFile || of.isProgram()) {
				// dump whole thing
				byte[] contents = new byte[file.getFileSize()];
				file.readContents(contents, 0, 0, contents.length);
				lines.add(contents);
			} else {
				// else, read records
				byte[] record = new byte[256];
				ByteMemoryAccess access = new ByteMemoryAccess(record, 0);
				while (true) {
					access.offset = 0;
					int len = 0;
					try {
						len = of.readRecord(access, file.getRecordLength());
					} catch (DsrException e) {
						if (e.getErrorCode() == PabConstants.e_endoffile) {
							break;
						} else {
							throw e;
						}
					}
					lines.add(Arrays.copyOfRange(record, 0, len));
				}
			}
		} finally {
			of.close();
		}
		return lines;
		
	}
}
