/*
  RedrawBlock.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.video;

/** 
 * This class represents a single block which needs to be
 * redrawn.
 * @author ejs
 *
 */
public class RedrawBlock {
	/** pixel offset, 0=top */
	public int r;
	/** pixel offset, 0=left */
	public int c;
	/** width of block */
	public int w;
	/** height of block */
	public int h;
}