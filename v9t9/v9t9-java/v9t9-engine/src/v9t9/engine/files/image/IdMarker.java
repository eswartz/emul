/*
  IdMarker.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.files.image;

public class IdMarker {
	public int idoffset;
	public int dataoffset;
	
	public byte trackid;
	public byte sectorid;
	public byte sideid;
	public byte sizeid;
	public short crcid;
	@Override
	public String toString() {
		return "IdMarker [trackid=" + trackid + ", sectorid=" + sectorid
				+ ", sideid=" + sideid + "]";
	}
	
	
}