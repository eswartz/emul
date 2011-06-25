/**
 * Mar 1, 2011
 */
package v9t9.emulator.hardware.dsrs.realdisk;

public class IdMarker {
	public int idoffset;
	public int dataoffset;
	
	public byte trackid;
	public byte sectorid;
	public byte sideid;
	public byte sizeid;
	public short crcid;
}