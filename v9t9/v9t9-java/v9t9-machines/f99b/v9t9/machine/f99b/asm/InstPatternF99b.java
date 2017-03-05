/*
  InstPatternF99b.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.f99b.asm;

public class InstPatternF99b {

	public final static int NONE = 0;
	public final static int IMM = 1;
	
	public int opcode = -1;
	
	public final int op1;
	
	InstPatternF99b(int op1) {
		this.op1 = op1;
	}
	InstPatternF99b() {
		this(NONE);
	}
	/**
	 * @param mopIdx
	 * @return
	 */
	public int op(int mopIdx) {
		switch (mopIdx) {
		case 1: return op1;
		}
		return NONE;
	}
}