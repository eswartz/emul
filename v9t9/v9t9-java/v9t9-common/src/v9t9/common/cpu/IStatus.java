/*
  IStatus.java

  (c) 2005-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.cpu;

public interface IStatus {

	// Status setting flags
	public static final int stset_NONE = 0; // status not affected
	public static final int stset_ALL = 1; // all bits changed
	public static final int stset_INT = 2; // interrupt mask

	String toString();

	void copyTo(IStatus copy);

	short flatten();

	void expand(short stat);

	/** is arith greater */
	boolean isLT();

	/** is logical lower or equal */
	boolean isLE();

	/** is logical lower */
	boolean isL();

	/** is equal */
	boolean isEQ();

	/** is not equal */
	boolean isNE();

	/** is logical higher or equal */
	boolean isHE();

	/** is arith greater or equal */
	boolean isGT();

	/** is logical higher */
	boolean isH();

	/** is carry */
	boolean isC();

	int getIntMask();

}