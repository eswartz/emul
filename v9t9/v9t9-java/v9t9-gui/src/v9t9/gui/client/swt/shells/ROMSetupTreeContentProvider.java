/**
 * 
 */
package v9t9.gui.client.swt.shells;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import v9t9.common.files.IPathFileLocator;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.modules.IModule;

/**
 * @author ejs
 *
 */
public class ROMSetupTreeContentProvider implements ITreeContentProvider {

	/**
	 * 
	 */
	private static final String MODULES_DETECTED = "Modules Detected";
	/**
	 * 
	 */
	private static final String OPTIONAL_ROMS = "Optional ROMs";
	/**
	 * 
	 */
	private static final String REQUIRED_ROMS = "Required ROMs";
	private MemoryEntryInfo[] requiredRoms;
	private MemoryEntryInfo[] optionalRoms;
	private IMachine machine;
	private List<IModule> detectedModules = new ArrayList<IModule>();
	private Map<Object, Object> parentMap = new HashMap<Object, Object>();
	private Thread refreshThread;
	private boolean needsRefresh;
	private Viewer viewer;
	static final URI databaseURI = URI.create("temp_modules.xml");

	/**
	 * @param requiredRoms
	 * @param optionalRoms
	 * @param machine
	 */
	public ROMSetupTreeContentProvider(MemoryEntryInfo[] requiredRoms,
			MemoryEntryInfo[] optionalRoms, IMachine machine) {
		this.requiredRoms = requiredRoms;
		this.optionalRoms = optionalRoms;
		this.machine = machine;
		refresh();
	}
	
	public synchronized void refresh() {
		if (refreshThread == null || !refreshThread.isAlive()) {
			refreshThread = new Thread() {
				@Override
				public void run() {
					synchronized (ROMSetupTreeContentProvider.this) {
						needsRefresh = false;
						detectedModules.clear();
						infoPathMap.clear();
						modulePathMap.clear();
					}
					for (URI uri : machine.getRomPathFileLocator().getSearchURIs()) {
						try {
							Collection<IModule> modules = machine.scanModules(databaseURI, new File(uri));
							detectedModules.addAll(modules);
						} catch (IllegalArgumentException e) {
							// ignore
						}
					}
					synchronized (ROMSetupTreeContentProvider.this) {
						ROMSetupTreeContentProvider.this.notifyAll();
						
						if (viewer != null) {
							viewer.getControl().getDisplay().asyncExec(new Runnable() {
								public void run() {
									viewer.refresh();
									((TreeViewer) viewer).expandToLevel(2);
								}
							});
						}
						refreshThread = null;
						if (needsRefresh) {
							refresh();
						}
					}
				}
			};
			refreshThread.setDaemon(true);
			refreshThread.start();
		} else {
			needsRefresh = true;
		}
	}
	
	/**
	 * @return the requiredRoms
	 */
	public MemoryEntryInfo[] getRequiredRoms() {
		return requiredRoms;
	}
	/**
	 * @return the optionalRoms
	 */
	public MemoryEntryInfo[] getOptionalRoms() {
		return optionalRoms;
	}
	/**
	 * @return the machine
	 */
	public IMachine getMachine() {
		return machine;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = newInput != null ? viewer : null;
		viewer.refresh();
		parentMap.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		return new Object[] { REQUIRED_ROMS, OPTIONAL_ROMS, MODULES_DETECTED };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement == REQUIRED_ROMS) {
			for (MemoryEntryInfo info : requiredRoms)
				parentMap.put(info, REQUIRED_ROMS);
			return requiredRoms;
		} else if (parentElement == OPTIONAL_ROMS) {
			for (MemoryEntryInfo info : optionalRoms)
				parentMap.put(info, OPTIONAL_ROMS);
			return optionalRoms;
		} else if (parentElement == MODULES_DETECTED) {
			synchronized (this) {
				return detectedModules.toArray();
			}
		} 
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object element) {
		if (element == requiredRoms)
			return REQUIRED_ROMS;
		if (element == optionalRoms)
			return OPTIONAL_ROMS;
		if (element instanceof IModule)
			return MODULES_DETECTED;
		
		return parentMap.get(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(Object element) {
		return element == REQUIRED_ROMS || element == OPTIONAL_ROMS || element == MODULES_DETECTED;
	}

	private Map<MemoryEntryInfo, URI> infoPathMap = new HashMap<MemoryEntryInfo, URI>();
	private Map<IModule, File[]> modulePathMap = new HashMap<IModule, File[]>();

	/**
	 * @param info
	 * @return
	 */
	public URI getPathFor(MemoryEntryInfo info) {
		URI uri = infoPathMap.get(info);
		if (!infoPathMap.containsKey(info)) {
			IPathFileLocator locator = machine.getRomPathFileLocator();
			uri = locator.findFile(machine.getSettings(), info);
			infoPathMap.put(info, uri);
		}
		return uri;
	}

	/**
	 * @param module
	 * @return
	 */
	public File[] getPathsFor(IModule module) {
		File[] paths = modulePathMap.get(module);
		if (paths == null) {
			Collection<File> usedFiles = module.getUsedFiles(machine.getRomPathFileLocator());
			paths = (File[]) usedFiles.toArray(new File[usedFiles.size()]);
			modulePathMap.put(module, paths);
		}
		return paths;
	}

	/**
	 * @param element
	 * @return
	 */
	public boolean isRequired(MemoryEntryInfo element) {
		return getParent(element) == REQUIRED_ROMS;
	}

}
