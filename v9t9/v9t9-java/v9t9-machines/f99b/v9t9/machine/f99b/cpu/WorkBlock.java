/**
 * 
 */
package v9t9.machine.f99b.cpu;

import v9t9.common.memory.IMemoryDomain;

public class WorkBlock {
	public short pc;
	
	public IMemoryDomain domain;
	
	
	public WorkBlock(short pc, IMemoryDomain domain) {
		super();
		this.pc = pc;
		this.domain = domain;
	}
	/**
	 * @return
	 */
	public int nextByte() {
		return domain.readByte(pc++) & 0xff;
	}
	/**
	 * @return
	 */
	public int nextWord() {
		return (domain.readByte(pc++) << 8) | domain.readByte(pc++) & 0xff;
	}
}