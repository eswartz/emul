/**
 * 
 */
package v9t9.emulator.runtime.cpu;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;

/**
 * Track the metrics of the CPU
 * @author Ed
 *
 */
public class CpuMetrics {
 
	public interface IMetricsListener {
		void metricsChanged();
	}
	
	static public class MetricEntry {
		int instructions;
		int cycles;
		int vdpInterrupts;
		int honoredInterrupts;
		int compiledInstructions;
		int switches;
		int compiles;
		private final int idealCycles;
		public MetricEntry(int instructions, int cycles, int idealCycles, 
				int vdpInterrupts, int honoredInterrupts, 
				int compiledInstructions, int switches,
				int compiles) {
			super();
			this.instructions = instructions;
			this.cycles = cycles;
			this.idealCycles = idealCycles;
			this.vdpInterrupts = vdpInterrupts;
			this.honoredInterrupts = honoredInterrupts;
			this.compiledInstructions = compiledInstructions;
			this.switches = switches;
			this.compiles = compiles;
		}
		public void dump() {
	        System.out.println(toSummary());
	        	
		}
		public int getIdealCycles() {
			return idealCycles;
		}
		public int getCycles() {
			return cycles;
		}
		public int getIdealInterrupts() {
			return vdpInterrupts;
		}
		public int getInterrupts() {
			return honoredInterrupts;
		}
		public String toSummary() {
			double compiled = (double)compiledInstructions / (double)instructions;
			int compileAvg = ((int) (compiled * 10000));
			return "# instructions / second: " + instructions
    		+ " (cycles = " + cycles 
    		+ (compiledInstructions > 0 ? 
    		"; " + compileAvg / 100 + "." + compileAvg % 100 + "% compiled, " 
    		+ switches + " context switches, " + compiles + " compiles)" : ")")
    		+ "; VDP Interrupts = " +vdpInterrupts + " (honored = " + honoredInterrupts + ")";			
		}
	}
	

	private ListenerList metricsListeners;
	
	private long nLastCycleCount;
	private ArrayList<MetricEntry> entries;
	public CpuMetrics() {
		entries = new ArrayList<MetricEntry>();
		metricsListeners = new ListenerList();
		reset();
	}
	
	public void addListener(IMetricsListener listener) {
		metricsListeners.add(listener);
	}
	
	public void removeListener(IMetricsListener listener) {
		metricsListeners.remove(listener);
	}
	private void reset() {
		nLastCycleCount = 0;		
	}
	public MetricEntry log(long instructions, long totalCycles, int idealCycles,
			int vdpInterrupts, int honoredInterrupts, long compiledInstructions, long switches, long compiles) {
        if (instructions == 0) {
        	return null;
        }

        MetricEntry entry = new MetricEntry((int)instructions, 
        		(int)(totalCycles - nLastCycleCount), idealCycles,
        		vdpInterrupts, honoredInterrupts, (int)compiledInstructions, 
        		(int)switches, (int)compiles);
        
        //System.out.println(entry.toSummary());
        
        if (entries.size() >= 1024)
        	entries = new ArrayList<MetricEntry>(entries.subList(entries.size() - 512, entries.size()));
        
		entries.add(entry);
		
        nLastCycleCount = totalCycles;
        
        fireListeners();
        return entry;
	}
	private void fireListeners() {
		for (Object obj : metricsListeners.getListeners()) {
			IMetricsListener listener = (IMetricsListener) obj;
			listener.metricsChanged();
		}
	}

	public MetricEntry[] getLastEntries(int x) {
		int idx = Math.max(0, entries.size() - x);
		List<MetricEntry> subList = entries.subList(idx, entries.size());
		return (MetricEntry[]) subList.toArray(new MetricEntry[subList.size()]);
	}

	public MetricEntry getEntry(int i, int of) {
		int idx = Math.max(0, entries.size() - of);
		if (idx + i >= 0  && idx + i < entries.size())
			return entries.get(idx + i);
		
		return null;
	}

	public void resetLastCycleCount() {
		nLastCycleCount = 0;
	}
	
}
