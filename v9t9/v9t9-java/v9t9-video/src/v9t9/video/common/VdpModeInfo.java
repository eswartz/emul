/*
  VdpModeInfo.java

  (c) 2008-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.common;

public class VdpModeInfo
{
	public VdpArea 	screen;	 	// screen image table
	public VdpArea 	patt; 				// pattern definition table
	public VdpArea 	color; 				// color definition table
	public VdpArea 	sprite; 			// sprite definition table
	public VdpArea 	sprpat;				// sprite pattern definition table
	public VdpModeInfo() {
		screen = new VdpArea();
		patt = new VdpArea();
		color = new VdpArea();
		sprite = new VdpArea();
		sprpat = new VdpArea();
	}
}