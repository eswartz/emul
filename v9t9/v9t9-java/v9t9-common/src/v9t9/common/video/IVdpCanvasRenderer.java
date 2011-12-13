/**
 * 
 */
package v9t9.common.video;


/**
 * @author ejs
 *
 */
public interface IVdpCanvasRenderer {

	boolean update();
	
	VdpModeInfo getModeInfo();
	

	/**
	 * @return
	 */
	IVdpCanvas getCanvas();

}
