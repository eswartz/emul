/**
 * 
 */
package v9t9.common.video;

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