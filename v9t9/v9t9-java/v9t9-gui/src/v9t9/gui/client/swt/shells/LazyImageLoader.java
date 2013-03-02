/*
  LazyImageLoader.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import ejs.base.utils.ListenerList;
import ejs.base.utils.ListenerList.IFire;

/**
 * @author ejs
 *
 */
public class LazyImageLoader {
	private static Map<String, Image> loadedImages = new HashMap<String, Image>();
	
	interface ILazyImageAdjuster {
		Image adjustImage(Object element, URI imageURI, Image image);
	}
	interface ILazyImageLoadedListener {
		void imageLoaded(Object element, URI imageURI, Image image);
	}
	
	private StructuredViewer viewer;
	private final ExecutorService executor;
	private final Image defaultImage;

	private ListenerList<ILazyImageLoadedListener> listeners = new ListenerList<ILazyImageLoadedListener>();
	
	public LazyImageLoader(StructuredViewer viewer, ExecutorService executor, Image defaultImage) {
		this.viewer = viewer;
		this.executor = executor;
		this.defaultImage = defaultImage;
	}
	
	public void addListener(ILazyImageLoadedListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(ILazyImageLoadedListener listener) {
		listeners.remove(listener);
	}
	
	public Image findOrLoadImage(final Object element, final URI theImageURI) {
		return findOrLoadImage(element, theImageURI, null);
	}

	public Image findOrLoadImage(final Object element, final URI theImageURI, ILazyImageAdjuster adjuster) {
		
		Image image;
		synchronized (loadedImages) {
			
			image = loadedImages.get(theImageURI.toString());
			if (image == null) {
				scheduleImageLoad(element, theImageURI, adjuster);
				
				return defaultImage;
			} else {
				return image;
			}
		}
		
	}
	
	/**
	 * @param theImageURI
	 * @param adjuster 
	 */
	protected void scheduleImageLoad(final Object element, final URI theImageURI, final ILazyImageAdjuster adjuster) {
		Runnable runnable = new Runnable() {
			public void run() {
				final Image image = loadImage(element, theImageURI, adjuster);
				
				synchronized (loadedImages) {
					loadedImages.put(theImageURI.toString(), image);
				}
				
				listeners.fire(new IFire<LazyImageLoader.ILazyImageLoadedListener>() {

					@Override
					public void fire(ILazyImageLoadedListener listener) {
						listener.imageLoaded(element, theImageURI, image);
					}
				});
			}
		};
		
		executor.submit(runnable);
	}

	/**
	 * @param theImageURI
	 */
	protected Image loadImage(final Object element, final URI theImageURI, ILazyImageAdjuster adjuster) {
		Display display = viewer.getControl().getDisplay();
		if (display == null)
			return null;

		//long start = System.currentTimeMillis(); 
		Image image = null;
		InputStream is = null;
		try {
			is = theImageURI.toURL().openStream();
			image = new Image(viewer.getControl().getDisplay(), is);
			
			if (adjuster != null) {
				image = adjuster.adjustImage(element, theImageURI, image);
			}
			
			return image;
			
		} catch (IOException e) {
			e.printStackTrace();
			
			return null;
		} finally {
			try {
				if (is != null) is.close();
			} catch (IOException e) {
			}
			//long end = System.currentTimeMillis();
			//System.out.println("... image load+adjust took " + (end - start));
		}		
	}

}
 