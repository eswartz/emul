/**
 * 
 */
package v9t9.gui.video;

import v9t9.common.video.VdpModeInfo;

/**
 * @author ejs
 *
 */
public interface IVdpModel {

	int getRegisterCount();
	String getRegisterName(int reg);
	String getRegisterTooltip(int reg);
	byte getRegister(int reg);
	void setRegister(int reg, byte value);

	VdpModeInfo getModeInfo();
	
	/**
	 * Tell whether interlacing is active.
	 * 
	 * For use in rendering, we need to know whether raw R9_IL (interlace) bit is set
	 * and also the R9_EO (even/odd) bit is set, which would provide the page flipping
	 * required to *see* two pages.  Finally, the "odd" graphics page must be visible
	 * for the flipping and interlacing to occur.
	 * @return
	 */
	
	public boolean isInterlacedEvenOdd();

	/**
	 * @return
	 */
	int getGraphicsPageSize();

	/**
	 * @return
	 */
	int getModeNumber();

}
