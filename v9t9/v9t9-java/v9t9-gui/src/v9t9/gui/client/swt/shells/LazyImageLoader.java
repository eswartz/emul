/*
  LazyImageLoader.java

  (c) 2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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
 