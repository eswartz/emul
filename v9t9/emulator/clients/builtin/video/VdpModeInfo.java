/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

class VdpModeInfo
{
	VdpArea 	screen;	 	// screen image table
	VdpArea 	patt; 				// pattern definition table
	VdpArea 	color; 				// color definition table
	VdpArea 	sprite; 			// sprite definition table
	VdpArea 	sprpat;				// sprite pattern definition table
	public VdpModeInfo() {
		screen = new VdpArea();
		patt = new VdpArea();
		color = new VdpArea();
		sprite = new VdpArea();
		sprpat = new VdpArea();
		
	}
}