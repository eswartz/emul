/*
  MetricEntry.java

  (c) 2011-2012 Edward Swartz

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