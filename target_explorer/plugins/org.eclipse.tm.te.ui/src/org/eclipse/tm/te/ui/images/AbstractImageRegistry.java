/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.images;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tm.te.ui.activator.UIPlugin;
import org.eclipse.tm.te.ui.nls.Messages;
import org.osgi.framework.Bundle;



/**
 * Target Explorer: Abstract image registry that allows for defining fallback paths for images.
 */
public abstract class AbstractImageRegistry extends ImageRegistry {
	private List<ImageRegistry> fDelegates = new ArrayList<ImageRegistry>();
	private Map<String,String> fPlugins = new HashMap<String,String>();
	private Map<String,String[]> fLocations = new HashMap<String,String[]>();
	private URL fBaseUrl;

	protected AbstractImageRegistry(Plugin plugin) {
		fBaseUrl = plugin.getBundle().getEntry("/"); //$NON-NLS-1$
	}

	/**
	 * Adds the given image registry as delegate. Delegates are queried if
	 * an image or image descriptor cannot be found locally. If the image
	 * registry delegate had been added before, the method will do nothing.
	 *
	 * @param registry The image registry. Must be not <code>null</code>.
	 */
	protected final void addImageRegistryDelegate(ImageRegistry registry) {
		assert registry != null;
		if (!fDelegates.contains(registry)) fDelegates.add(registry);
	}

	/**
	 * Removes the given image registry from the list of delegates.
	 *
	 * @param registry The image registry. Must be not <code>null</code>.
	 */
	protected final void removeImageRegistryDelegate(ImageRegistry registry) {
		assert registry != null;
		fDelegates.remove(registry);
	}

	/**
	 * Defines the key for a local image, that must be found below the icons directory
	 * in the plugin.
	 * @param key Key by which the image can be referred by.
	 * @param dir Directory relative to icons/
	 * @param name The name of the file defining the icon. The name will be used as
	 *   key.
	 */
	protected void localImage(String key, String dir, String name) {
		if (dir== null || dir.equals(""))//$NON-NLS-1$
			fLocations.put(key, new String[] {"icons/" + name}); //$NON-NLS-1$
		else
			fLocations.put(key, new String[] {"icons/" + dir + "/" + name}); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Defines the key for a non-local image, that must be found below the icons directory
	 * of some plugin.
	 * @param key Key by which the image can be referred by.
	 * @param plugin The plugin id, where the icon is searched.
	 * @param dirs A couple of directories below icons/ in the plugin. If loading fails,
	 * the next dir will be taken as fallback.
	 * @param name The name of the file defining the icon. The name will be used as
	 *   key.
	 */
	protected void externalImage(String key, String plugin, String[] dirs, String name) {
    	if (plugin != null) {
    		fPlugins.put(key, plugin);
    	}
    	String[] locations = new String[dirs.length];
    	for (int i = 0; i < dirs.length; i++) {
			String dir = dirs[i];
			if (dir== null || dir.equals(""))//$NON-NLS-1$
				locations[i] = "icons/" + name; //$NON-NLS-1$
			else
				locations[i] = "icons/" + dir + "/" + name; //$NON-NLS-1$ //$NON-NLS-2$
    	}
    	fLocations.put(key, locations);
	}

	final private Image internalDoGet(String key) {
		// First query the parent (local) image registry if
		// an image for the given key is registered.
		Image i = super.get(key);
		if (i != null) return i;

		// If no image had been returned, try the delegates
		for (ImageRegistry delegate : fDelegates) {
			i = delegate.get(key);
			if (i != null) break;
		}

		return i;
	}

	final private ImageDescriptor internalDoGetDescriptor(String key) {
		// First query the parent (local) image registry if
		// an image for the given key is registered.
		ImageDescriptor d = super.getDescriptor(key);
		if (d != null) return d;

		// If no image had been returned, try the delegates
		for (ImageRegistry delegate : fDelegates) {
			d = delegate.getDescriptor(key);
			if (d != null) break;
		}

		return d;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.resource.ImageRegistry#get(java.lang.String)
	 */
	@Override
	final public Image get(String key) {
	    Image i = internalDoGet(key);
	    if (i != null) {
	        return i;
	    }

	    ImageDescriptor d = createFileImageDescriptor(key);
	    if (d != null) {
	        put(key, d);
	        return internalDoGet(key);
	    }
	    return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.resource.ImageRegistry#getDescriptor(java.lang.String)
	 */
	@Override
	final public ImageDescriptor getDescriptor(String key) {
	    ImageDescriptor d = internalDoGetDescriptor(key);
	    if (d != null) {
	        return d;
	    }

	    d = createFileImageDescriptor(key);
	    if (d != null) {
	        put(key, d);
	        return d;
	    }
	    return null;
	}

	private ImageDescriptor createFileImageDescriptor(String key) {
		URL url = fBaseUrl;
		String pluginId =  fPlugins.get(key);
		if (pluginId != null) {
			Bundle bundle= Platform.getBundle(pluginId);
			if (bundle != null) {
				url = bundle.getEntry("/"); //$NON-NLS-1$
			}
		}
		String[] locations= fLocations.get(key);
		if (locations != null) {
			for (int i = 0; i < locations.length; i++) {
				String loc = locations[i];
				URL full;
				try {
					full = new URL(url, loc);
					ImageDescriptor candidate = ImageDescriptor.createFromURL(full);
					if (candidate != null && candidate.getImageData() != null) {
						return candidate;
					}
				} catch (MalformedURLException e) {
					IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
					                            Messages.AbstractImageRegistry_error_malformedImage, e);
					UIPlugin.getDefault().getLog().log(status);
				} catch (SWTException e) {
					// try the next one.
				}
			}
		}
	    return null;
	}

	/**
	 * Get a shared Image for a given descriptor
	 */
	public Image getSharedImage(AbstractImageDescriptor d) {
		String key = d.getKey();
		Image shared = super.get(key);
		if (shared != null) {
			return shared;
		}
		put(key, d);
		return super.get(key);
	}
}
