/**
 * 
 */
package v9t9.gui.client.swt.shells;

import java.io.File;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import v9t9.common.machine.IMachine;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.modules.IModule;
import ejs.base.utils.TextUtils;

/**
 * @author ejs
 *
 */
public class ROMSetupLabelProvider extends LabelProvider implements
		ITableLabelProvider, ITableFontProvider, ITableColorProvider {

	private ROMSetupTreeContentProvider contentProvider;
	private IMachine machine;
	
	/**
	 * @param contentProvider
	 */
	public ROMSetupLabelProvider(IMachine machine, ROMSetupTreeContentProvider contentProvider) {
		this.machine = machine;
		this.contentProvider = contentProvider;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	protected String uriToFile(URI uri) {
		if (uri != null) {
			try {
				return new File(uri).getName();
			} catch (IllegalArgumentException e) {
				String path = uri.toString();
				int idx = path.lastIndexOf('/');
				return path.substring(idx+1);
			}
		} else {
			return "(not found)";
		}
	}
	protected String uriToDir(URI uri) {
		if (uri != null) {
			try {
				return new File(uri).getParent();
			} catch (IllegalArgumentException e) {
				String path = uri.toString();
				int idx = path.lastIndexOf('/');
				return idx >= 0 ? path.substring(0, idx) : path;
			}
		} else {
			return "(not found)";
		}
	}
	protected String uriToPath(URI uri) {
		if (uri != null) {
			try {
				return new File(uri).getAbsolutePath();
			} catch (IllegalArgumentException e) {
				return uri.toString();
			}
		} else {
			return "(not found)";
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (columnIndex == 0) {
			if (element instanceof String) {
				return (String) element;
			}
			if (element instanceof IModule) {
				IModule mod = (IModule) element;
				return mod.getName();
			}
			if (element instanceof MemoryEntryInfo) {
				MemoryEntryInfo info = (MemoryEntryInfo) element;
				return info.getName();
			}
		}
		else if (columnIndex == 1) {
			if (element instanceof IModule) {
				IModule module = (IModule) element;
				File[] files = contentProvider.getPathsFor(module);
				if (files == null)
					return "(not found)";
				StringBuilder sb = new StringBuilder();
				for (File file : files) {
					if (sb.length() > 0)
						sb.append(", ");
					sb.append(file.getName());
				}
				return sb.toString();
			}
			if (element instanceof MemoryEntryInfo) {
				MemoryEntryInfo info = (MemoryEntryInfo) element;
				URI uri = contentProvider.getPathFor(info);
				if (uri != null)
					return uriToFile(uri);
				else
					return info.getResolvedFilename(machine.getSettings());
			}
		}
		else if (columnIndex == 2) {
			if (element instanceof IModule) {
				IModule module = (IModule) element;
				File[] files = contentProvider.getPathsFor(module);
				
				if (files == null)
					return "(not found)";
				
				Set<String> dirs = new LinkedHashSet<String>(); 
				for (File file : files) {
					dirs.add(file.getParent());
				}
				return TextUtils.catenateStrings(dirs, ", ");
			}
			if (element instanceof MemoryEntryInfo) {
				MemoryEntryInfo info = (MemoryEntryInfo) element;
				URI uri = contentProvider.getPathFor(info);
				return uriToDir(uri);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object, int)
	 */
	@Override
	public Color getForeground(Object element, int columnIndex) {
		if (element instanceof MemoryEntryInfo) {
			MemoryEntryInfo info = (MemoryEntryInfo) element;
			if (contentProvider.getPathFor(info) == null) {
				if (contentProvider.isRequired(info)) {
					return Display.getDefault().getSystemColor(SWT.COLOR_RED);
				}
			} else if (columnIndex == 0) {
				return Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
				
			}
		}
		else if (element instanceof IModule) {
			if (columnIndex == 0) {
				return Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang.Object, int)
	 */
	@Override
	public Color getBackground(Object element, int columnIndex) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableFontProvider#getFont(java.lang.Object, int)
	 */
	@Override
	public Font getFont(Object element, int columnIndex) {
		if (element instanceof MemoryEntryInfo
				&& contentProvider.getPathFor((MemoryEntryInfo) element) == null) {
			return JFaceResources.getFontRegistry().getItalic(JFaceResources.DIALOG_FONT);
		}
		if (element instanceof String) {
			return JFaceResources.getBannerFont();
		}
		return null;
	}

}
