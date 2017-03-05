/*
  VdpTouchHandlerBlock.java

  (c) 2008-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video;

public class VdpTouchHandlerBlock
{
	public VdpTouchHandler screen;	// modified screen image table
	public VdpTouchHandler	patt;				// modified pattern definition table
	public VdpTouchHandler	color;				// modified color definition table
	public VdpTouchHandler	sprite;				// modified sprite definition table
	public VdpTouchHandler	sprpat;				// modified sprite pattern table
}