/**
 * 
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