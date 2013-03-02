/*
  CpuMetrics.java

  (c) 2011 Edward Swartz

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
package v9t9.common.cpu;

import java.util.ArrayList;
import java.util.List;

import ejs.base.utils.ListenerList;
import ejs.base.utils.ListenerList.IFire;


/**
 * Track the metrics of the CPU
 * @author Ed
 *
 */
public class CpuMetrics implements ICpuMetrics {
 
	public interface IMetricsListener {
		void metricsChanged();
	}
	
	private ListenerList<IMetricsListener> metricsListeners;
	
	private ArrayList<MetricEntry> entries;
	public CpuMetrics() {
		entries = new ArrayList<MetricEntry>();
		metricsListeners = new ListenerList<IMetricsListener>();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.ICpuMetrics#addListener(v9t9.emulator.runtime.cpu.CpuMetrics.IMetricsListener)
	 */
	@Override
	public void addListener(IMetricsListener listener) {
		metricsListeners.add(listener);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.ICpuMetrics#removeListener(v9t9.emulator.runtime.cpu.CpuMetrics.IMetricsListener)
	 */
	@Override
	public void removeListener(IMetricsListener listener) {
		metricsListeners.remove(listener);
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.ICpuMetrics#log(long, long, int, int, int, long, long, long)
	 */
	@Override
	public void log(MetricEntry entry) {
        if (entry.getCycles() == 0) {
        	return;
        }
        
        //System.out.println(entry.toSummary());
        
        synchronized (entries) {
	        if (entries.size() >= 1024) 
	        	entries.subList(0, entries.size() - 512).clear();
	        	//entries = new ArrayList<MetricEntry>(entries.subList(entries.size() - 512, entries.size()));
	        
			entries.add(entry);
        }
		
        //nLastCycleCount = totalCycles;
        
        fireListeners();
	}
	private void fireListeners() {
		metricsListeners.fire(new IFire<CpuMetrics.IMetricsListener>() {

			@Override
			public void fire(IMetricsListener listener) {
				try {
					listener.metricsChanged();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		});
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.ICpuMetrics#getLastEntries(int)
	 */
	@Override
	public MetricEntry[] getLastEntries(int x) {
		synchronized (entries) {
			int idx = Math.max(0, entries.size() - x);
			List<MetricEntry> subList = entries.subList(idx, entries.size());
			return subList.toArray(new MetricEntry[subList.size()]);
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.ICpuMetrics#getEntry(int, int)
	 */
	@Override
	public MetricEntry getEntry(int i, int of) {
		synchronized (entries) {
			int idx = Math.max(0, entries.size() - of);
			if (idx + i >= 0  && idx + i < entries.size())
				return entries.get(idx + i);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.ICpuMetrics#resetLastCycleCount()
	 */
	//@Override
	//public void resetLastCycleCount() {
	//	nLastCycleCount = 0;
	//}
	
}
