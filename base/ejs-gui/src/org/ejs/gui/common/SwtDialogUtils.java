/*
  SwtDialogUtils.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package org.ejs.gui.common;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;

/**
 * @author ejs
 *
 */
public class SwtDialogUtils {

	public static void showErrorMessage(Shell shell, String title, String msg) {
		MessageDialog.openError(shell, title, msg);
	}

	public static String openFileSelectionDialog(Shell shell, String title, String directory,
			String fileName, boolean isSave, String[] extensions) {
		FileDialog dialog = new FileDialog(shell, isSave ? SWT.SAVE : SWT.OPEN);
		dialog.setText(title);
		dialog.setFilterPath(directory);
		dialog.setFileName(fileName);
		
		if (extensions != null) {
			String[] exts = new String[extensions.length];
			String[] names = new String[extensions.length];
			int idx = 0;
			for (String extension : extensions) {
				String[] split = extension.split("\\|");
				exts[idx] = "*." + split[0];
				names[idx] = split[1];
				idx++;
			}
			dialog.setFilterExtensions(exts);
			dialog.setFilterNames(names);
		}
		String filename = dialog.open();
		
		if (filename != null && extensions != null) {
			int extIdx = new File(filename).getName().lastIndexOf('.');
			if (extIdx < 0) {
				filename += '.' + dialog.getFilterExtensions()[dialog.getFilterIndex()].substring(2);
			}
		}
		return filename;
	}

	public static File getUniqueFile(String filepathBase) {
		File fileBase = new File(filepathBase);
		File dir = fileBase.getParentFile();
		String base = fileBase.getName();
		int extPtr = base.lastIndexOf('.');
		if (extPtr < 0) extPtr = base.length();
		String ext = base.substring(extPtr);
		base = base.substring(0, extPtr);
		
		File saveFile = null; 
		for (int count = 0; count < 10000; count++) {
			saveFile = new File(dir, base + (count != 0 ? "" + count : "") + ext);
			if (!saveFile.exists())
				break;
		}
		if (saveFile.exists())
			return null;
		return saveFile;
	}

	public static void showMenu(Menu menu, final Control parent, final int x, final int y) {
		SwtUtils.runMenu(parent, x, y, menu);
		menu.dispose();		
	}

	/**
	 * Sometimes a viewer does not get populated until
	 * some time after its input is set, making {@link StructuredViewer#reveal(Object)}
	 * useless.  This routine reveals a selection only after
	 * the viewer is actually populated.
	 */
	public static void revealOncePopulated(final Timer timer,
			int initialDelayMs,
			final StructuredViewer viewer,
			final Object selection) {
		// workaround: GTK does not realize the elements for a while
		final Control control = viewer.getControl();
		final TimerTask task = new TimerTask() {
			TimerTask xx = this;
			/* (non-Javadoc)
			 * @see java.util.TimerTask#run()
			 */
			@Override
			public void run() {
				if (control.isDisposed()) {
					xx.cancel();
					return;
				}
					
				control.getDisplay().asyncExec(new Runnable() {
					public void run() {
						boolean cancel = false;
						if (control.isDisposed()) {
							cancel = true;
						}
						else {
							boolean populated = false;
							if (control instanceof Tree &&
									((Tree) control).getItemCount() > 0 &&
									((Tree) control).getItem(0).getBounds().height > 0)
								populated = true;
							else if (control instanceof Table &&
									((Table) control).getItemCount() > 0 &&
									((Table) control).getItem(0).getBounds().height > 0)
								populated = true;
							
							if (populated) {
								viewer.reveal(selection);
								cancel = true;
							}
						}
						
						if (cancel) {
							xx.cancel();
						}
					}
				});
			}
		};
		timer.scheduleAtFixedRate(task, initialDelayMs, 500);		
	}

	public static void setEnabled(Control c, boolean b) {
		c.setEnabled(b);
		if (c instanceof Composite) {
			for (Control k : ((Composite) c).getChildren()) {
				setEnabled(k, b);
			}
		}
	}

}
