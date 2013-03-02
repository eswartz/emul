/*
  BaseImageButtonAreaHandler.java

  (c) 2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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