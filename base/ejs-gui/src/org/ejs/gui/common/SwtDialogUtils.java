/**
 * 
 */
package org.ejs.gui.common;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

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
		runMenu(parent, x, y, menu);
		menu.dispose();		
	}

	public static void runMenu(final Control parent, final int x, final int y,
			final Menu menu) {
		if (parent != null) {
			Point loc = parent.toDisplay(x, y); 
			menu.setLocation(loc);
		}
		System.out.println("position: " + menu.getParent().getLocation());
		menu.setVisible(true);
		
		Display display = parent.getShell().getDisplay();
		while (display.readAndDispatch()) /**/ ;
	
		while (!menu.isDisposed() && menu.isVisible()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
	}

}
