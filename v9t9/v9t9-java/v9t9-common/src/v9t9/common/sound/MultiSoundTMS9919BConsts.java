/*
  MultiSoundTMS9919BConsts.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.sound;

/**
 * <pre>
 * BASE = console chip
 * BASE + 2 = #1 std
 * BASE + 4 = #1 effects command
 * BASE + 6 = #1 effects data
 * BASE + 8 = #2 std
 * BASE + 10 = #2 effects command
 * BASE + 12 = #2 effects data
 * BASE + 14 = #3 std
 * BASE + 16 = #3 effects command
 * BASE + 18 = #3 effects data
 * BASE + 20 = #4 std
 * BASE + 22 = #4 effects
 * BASE + 24 = #4 effects data
 * </pre>
 * @author ejs
 *
 */
public class MultiSoundTMS9919BConsts extends TMS9919BConsts {
	public static String GROUP_NAME = "Multi TMS 9919B";

}
