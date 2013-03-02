/*
  InstTableCommon.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.asm;

/**
 * @author Ed
 *
 */
public class InstTableCommon {

	public static final int Idata = 0;
	public static final int Idsr = -2;
	public static final int Iticks = -3;
	public static final int Iemitchar = -4;
	public static final int Idbg = -5;
	public static final int Idbgf = -6;
	public static final int Ikysl = -7;
	public static final int Ibyte = -1;
	public static final int Idelete = -999;	// noop
	/** user instructions */
	public static final int Iuser = 100;

}
