/*
  ViewerUpdater.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

class ViewerUpdater extends Thread { 

	/**
	 * 
	 */
	private final ModuleSelector moduleSelector;

	/**
	 * @param moduleSelector
	 */
	ViewerUpdater(ModuleSelector moduleSelector) {
		this.moduleSelector = moduleSelector;
	}

	private Queue<Object> elements = new LinkedBlockingDeque<Object>();
	protected boolean firstRefresh = true;
	
	public void post(Object element) {
		elements.add(element);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		final List<Object> avail = new ArrayList<Object>();
		while (true) {
			// delay to gather more changes at once
			try {
				Thread.sleep(750);
			} catch (InterruptedException e) {
				return;
			}
			
			synchronized (avail) {
				while (!elements.isEmpty()) {
					final Object element = elements.poll();
					avail.add(element);
				}
			}
			if (!avail.isEmpty() && !this.moduleSelector.getDisplay().isDisposed()) {
				this.moduleSelector.getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (!ViewerUpdater.this.moduleSelector.getViewer().getControl().isDisposed()) {
							Object[] availarray;
							synchronized (avail) {
								availarray = avail.toArray();
								avail.clear();
							}
							ViewerUpdater.this.moduleSelector.getViewer().update(availarray, ModuleSelector.NAME_PROPERTY_ARRAY);
							
							// the node may have been filtered out
							//refreshFilters();
							
							if (firstRefresh) {
								firstRefresh = false;
								
								ViewerUpdater.this.moduleSelector.initFilter(ModuleSelector.lastFilter);

								ViewerUpdater.this.moduleSelector.hookActions();
							}								
							
						}
					}
				});
			}
			
		}
	}

}