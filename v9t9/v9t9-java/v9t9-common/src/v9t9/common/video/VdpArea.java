/**
 * 
 */
package v9t9.common.video;

import ejs.base.utils.HexUtils;

public class VdpArea
{
	public int	base, size;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Base: " + HexUtils.toHex4(base) + "; Size: " + HexUtils.toHex4(size);
	}
}