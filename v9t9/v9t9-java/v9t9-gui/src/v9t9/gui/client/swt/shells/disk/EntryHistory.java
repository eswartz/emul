/*
  EntryHistory.java

  (c) 2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.disk;

import ejs.base.utils.ListenerList;
import v9t9.common.settings.IStoredSettings;

/**
 * This tracks the history shared among multiple items
 * @author ejs
 *
 */
public class EntryHistory {

	private IStoredSettings settings;
	private ListenerList<IEntryHistoryListener> listeners = new ListenerList<IEntryHistoryListener>();

	public EntryHistory(IStoredSettings settings) {
		this.settings = settings;
	}
	
	public void addListener(IEntryHistoryListener listener) {
		listeners.add(listener);
	}
	public void removeListener(IEntryHistoryListener listener) {
		listeners.remove(listener);
	}

	public String[] getItems(String historyId) {
		return settings.getHistorySettings().getArray(historyId);
	}
	public void setHistory(final String historyId, final String[] history) {
		settings.getHistorySettings().put(historyId, history);
		
		listeners.fire(new ListenerList.IFire<IEntryHistoryListener>() {

			@Override
			public void fire(IEntryHistoryListener listener) {
				listener.historyChanged(historyId, history);
			}
		});
	}


}
