/*
  SimpleBreakpoint.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.cpu;

/**
 * @author ejs
 *
 */
public class SimpleBreakpoint implements IBreakpoint {

	private final int pc;
	private final boolean oneShot;

	public SimpleBreakpoint(int pc, boolean oneShot) {
		this.pc = pc;
		this.oneShot = oneShot;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (oneShot ? 1231 : 1237);
		result = prime * result + pc;
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleBreakpoint other = (SimpleBreakpoint) obj;
		if (oneShot != other.oneShot)
			return false;
		if (pc != other.pc)
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see v9t9.common.cpu.IBreakpoint#getPc()
	 */
	@Override
	public int getPc() {
		return pc;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.cpu.IBreakpoint#execute(v9t9.common.cpu.ICpuState)
	 */
	@Override
	public boolean execute(ICpuState cpu) {
		return false;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.cpu.IBreakpoint#isCompleted()
	 */
	@Override
	public boolean isCompleted() {
		return oneShot;
	}

}
