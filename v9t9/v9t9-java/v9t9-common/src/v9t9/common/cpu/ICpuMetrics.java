/*
  ICpuMetrics.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.cpu;

import v9t9.common.cpu.CpuMetrics.IMetricsListener;

/**
 * @author ejs
 *
 */
public interface ICpuMetrics {

	void addListener(IMetricsListener listener);

	void removeListener(IMetricsListener listener);

	void log(MetricEntry entry);

	MetricEntry[] getLastEntries(int x);

	MetricEntry getEntry(int i, int of);

	//void resetLastCycleCount();

}