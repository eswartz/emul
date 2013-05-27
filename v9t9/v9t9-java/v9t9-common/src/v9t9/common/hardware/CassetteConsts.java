/*
  CassetteConsts.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.hardware;

/**
 * @author ejs
 *
 */
public class CassetteConsts {

	/** 
	 * This is the offset in the cassette register bank for
	 * the current queued value of the cassette output (0=off, 1=on) 
	 */
	final public static int REG_OFFS_CASSETTE_OUTPUT = 0;
	/** 
	 * This is the offset in the cassette register bank for
	 * the state of the cassette #1 motor (0=off, 1=on) 
	 */
	final public static int REG_OFFS_CASSETTE_MOTOR_1 = 1;
	/** 
	 * This is the offset in the cassette register bank for
	 * the state of the cassette #2 motor (0=off, 1=on) 
	 */
	final public static int REG_OFFS_CASSETTE_MOTOR_2 = 2;
	
	public static final int REG_COUNT_CASSETTE = 3;
	
}
