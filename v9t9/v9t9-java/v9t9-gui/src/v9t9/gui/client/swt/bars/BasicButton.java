/*
  BasicButton.java

  (c) 2009-2011 Edward Swartz

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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;


public class BasicButton extends ImageButton {
	
	public BasicButton(IImageBar buttonBar, 
			int style,
			ImageProvider provider, int iconIndex, String tooltip) {
		super(buttonBar, SWT.NO_FOCUS | SWT.NO_RADIO_GROUP |style, provider, iconIndex, tooltip);
		
		addKeyListener(new KeyListener() {
			
			public void keyPressed(KeyEvent e) {
				e.doit = false;
			}

			public void keyReleased(KeyEvent e) {
				e.doit = false;
			}
			
		});
		addTraverseListener(new TraverseListener() {

			public void keyTraversed(TraverseEvent e) {
				e.doit = false;
			}
			
		});

		setFocusRestorer(buttonBar.getFocusRestorer());
	}
}