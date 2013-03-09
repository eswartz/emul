/**
 * 
 */
package v9t9.gui.client.swt.shells.modules;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import v9t9.common.modules.IModule;

/**
 * @author ejs
 *
 */
public class ModuleContentProvider implements ITreeContentProvider {

	private Map<URI, Collection<IModule>> modDb;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.modDb = (Map<URI, Collection<IModule>>) newInput;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(Object el) {
		return getChildren(el);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object el) {
		if (el instanceof Map) {
			return ((Map<?, ?>) el).keySet().toArray();
		} else if (el instanceof URI) {
			return modDb.get(el).toArray(); 
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object el) {
		if (el instanceof URI)
			return modDb;
		else if (el instanceof IModule)
			return ((IModule) el).getDatabaseURI();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(Object el) {
		return el instanceof Map || el instanceof URI;
	}

}
