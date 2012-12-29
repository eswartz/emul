/**
 * 
 */
package v9t9.gui.client.swt.bars;


/**
 * @author ejs
 *
 */
public abstract class BaseImageButtonAreaHandler implements IImageButtonAreaHandler {

	protected ImageButton button;

	/**
	 * 
	 */
	public BaseImageButtonAreaHandler() {
		super();
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.bars.IImageButtonAreaHandler#attach(v9t9.gui.client.swt.bars.ImageButton)
	 */
	@Override
	public void attach(ImageButton button) {
		this.button = button;
	}
	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.bars.IImageButtonAreaHandler#detach(v9t9.gui.client.swt.bars.ImageButton)
	 */
	@Override
	public void detach(ImageButton button) {
		this.button = null;
	}
	
	
	@Override
	public boolean isMenu() {
		return false;
	}

	@Override
	public void mouseEnter() {
	
	}

	@Override
	public void mouseHover() {
		
	}

	@Override
	public void mouseExit() {
	
	}

	@Override
	public boolean mouseDown(int button) {
		return false;
	}
	
	@Override
	public boolean mouseUp(int button) {
		return true;
	}

}