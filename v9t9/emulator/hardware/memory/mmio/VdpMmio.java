package v9t9.emulator.hardware.memory.mmio;

import v9t9.emulator.Machine.ConsoleMmioReader;
import v9t9.emulator.Machine.ConsoleMmioWriter;
import v9t9.engine.Client;
import v9t9.engine.VdpHandler;

public abstract class VdpMmio implements ConsoleMmioReader, ConsoleMmioWriter {

	protected int currentaccesscycles;
	protected VdpHandler vdpHandler;

	public VdpMmio() {
	}

	abstract public int getAddr();

	public void setClient(Client client) {
	}

	public int getMemoryAccessCycles() {
		return currentaccesscycles;
	}
	
	/** Set the number of extra access cycles */
	public void setMemoryAccessCycles(int i) {
		currentaccesscycles = i;
	}

	public void setVdpHandler(VdpHandler vdp) {
		this.vdpHandler = vdp;
	}

}