/**
 * 
 */
package v9t9.engine.demos.events;

/**
 * @author ejs
 *
 */
public class VdpV9938AccelCommandEvent extends VideoAccelCommandEvent {

	public static final String ID = "VdpV9938AccelCommand";

	/**
	 * @param code
	 */
	public VdpV9938AccelCommandEvent(int code) {
		super(code);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoEvent#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return ID;
	}
}
