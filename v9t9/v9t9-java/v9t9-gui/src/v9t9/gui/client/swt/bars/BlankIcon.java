/**
 * Mar 12, 2011
 */
package v9t9.gui.client.swt.bars;


/**
 * @author ejs
 *
 */
public class BlankIcon extends ImageBarChild {

	public BlankIcon(final IImageBar parentDrawer, int style) {
		super(parentDrawer, style);
		
		setCursor(null);
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.bars.ImageIconCanvas#isIconMouseable()
	 */
	@Override
	protected boolean isIconMouseable() {
		return false;
	}
	

}
