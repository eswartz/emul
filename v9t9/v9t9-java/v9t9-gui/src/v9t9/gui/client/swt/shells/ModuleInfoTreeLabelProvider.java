/**
 * 
 */
package v9t9.gui.client.swt.shells;

import java.net.URI;
import java.util.Map;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.modules.IModule;
import v9t9.gui.client.swt.shells.ModuleSelector.ErrorTreeNode;
import v9t9.gui.client.swt.shells.ModuleSelector.InfoTreeNode;
import ejs.base.properties.IProperty;
import ejs.base.utils.Pair;

class ModuleInfoTreeLabelProvider extends BaseLabelProvider implements ITableLabelProvider,
	ITableColorProvider, ITableFontProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	@Override
	public String getColumnText(Object nodeElement, int columnIndex) {
		TreeNode treeNode = (TreeNode) nodeElement;
		Object element = treeNode.getValue();
		switch (columnIndex) {
		case 0:
			if (element instanceof IProperty)
				return "Search Path Property";
			if (element instanceof IModule)
				return "Memory Entries";
			if (element instanceof MemoryEntryInfo)
				return "Expected Properties";
			//if (element instanceof StoredMemoryEntryInfo)
			//	return split(((StoredMemoryEntryInfo) element).uri.getPath()).second;
			if (element instanceof Map.Entry)
				return ((Map.Entry<?, ?>) element).getKey().toString();
			if (element instanceof Pair)
				return ((Pair<?, ?>) element).first.toString();
			if (element instanceof URI)
				return ((URI) element).toString();
			return element.toString();
		case 1:
			if (element instanceof IProperty)
				return ((IProperty) element).getName();
			if (element instanceof IModule)
				return null;
			if (element instanceof String)
				return element.toString();
			if (element instanceof MemoryEntryInfo)
				return null;
			//if (element instanceof StoredMemoryEntryInfo)
			//	return split(((StoredMemoryEntryInfo) element).uri.getPath()).first;
			if (element instanceof Map.Entry)
				return ((Map.Entry<?, ?>) element).getValue().toString();
			if (element instanceof Pair)
				return ((Pair<?, ?>) element).second.toString();
			if (element instanceof URI) {
				return treeNode instanceof ErrorTreeNode ? "missing" : "present";
			}
			return null;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object, int)
	 */
	@Override
	public Color getForeground(Object element, int columnIndex) {
		return element instanceof ErrorTreeNode 
		? Display.getDefault().getSystemColor(SWT.COLOR_RED) : null;
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
		if (columnIndex == 0 && element instanceof InfoTreeNode) 
			return JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT);
		
		if (columnIndex == 1 && ((TreeNode) element).getValue() instanceof IProperty)
			return JFaceResources.getTextFont();
		
		return null;
	}
	
	
}