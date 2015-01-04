/**
 * 
 */
package v9t9.engine.dsr.rs232;

import v9t9.common.dsr.IDeviceLabel;

/**
 * @author ejs
 *
 */
public enum RS232Controllers implements IDeviceLabel {
	RS232_ONLY("RS232 Only", "Use the RS232 serial-only DSR ROM"),
	RS232_PIO("RS232 + PIO", "Use the combined RS232 + PIO (parallel) DSR ROM");

	private final String label, tooltip;
	
	private RS232Controllers(String label, String tooltip) {
		this.label = label;
		this.tooltip = tooltip;
	}
	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IDeviceLabel#getLabel()
	 */
	@Override
	public String getLabel() {
		return label;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IDeviceLabel#getTooltip()
	 */
	@Override
	public String getTooltip() {
		return tooltip;
	}
}
