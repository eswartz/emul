/*
  MetricEntry.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.cpu;

public class MetricEntry {
	private long instructions;
	private long cycles;
	private int vdpInterrupts;
	private int honoredInterrupts;
	private long compiledInstructions;
	private int switches;
	private int compiles;
	private final int idealCycles;
	
	public MetricEntry(long instructions, long cycles,
			int idealCycles, 
			int vdpInterrupts, int honoredInterrupts, 
			long compiledInstructions, int switches,
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
	public int getIdealCycles() {
		return idealCycles;
	}
	public long getCycles() {
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